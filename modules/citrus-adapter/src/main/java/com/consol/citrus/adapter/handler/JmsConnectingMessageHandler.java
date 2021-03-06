/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.adapter.handler;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.JmsMessageConverter;
import com.consol.citrus.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.*;
import org.springframework.integration.Message;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.StringUtils;

import javax.jms.*;

/**
 * Message handler implementation forwarding incoming request to a JMS destination. The handler is
 * waiting for a reply message either on a static response destination or temporary queue destination.
 * 
 * The forwarding destination as well as the reply destination can be declared either as injected instance 
 * or by destination name.
 * 
 * A {@link JmsMessageCallback} implementation can manipulate the forwarded JMS request message before sending.
 * 
 * In case handler receives timeout and get no reply message on JMS destinations a fallback message handler
 * can provide a fallback response message.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class JmsConnectingMessageHandler implements MessageHandler, InitializingBean, DisposableBean {

    /** Forwarding destination */
    private Destination destination;
    
    /** Forwarding destination name */
    private String destinationName;

    /** Reply destination */
    private Destination replyDestination;
    
    /** Reply destination name */
    private String replyDestinationName;
    
    /** JMS connection factory */
    private ConnectionFactory connectionFactory;
    
    /** JMS connection */
    private Connection connection = null;
    
    /** JMS Session */
    private Session session = null;
    
    /** Time to wait for reply message */
    private long replyTimeout = 5000L;
    
    /** Message callback manipulating JMS request message */
    private JmsMessageCallback messageCallback;
    
    /** Fallback message handler in case no reply message was received */
    private MessageHandler fallbackMessageHandlerDelegate = new TimeoutProducingMessageHandler();
    
    /** Jms header mapper */
    private JmsHeaderMapper headerMapper = new DefaultJmsHeaderMapper();
    
    /** Jms message converter */
    private MessageConverter messageConverter = new SimpleMessageConverter();
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JmsConnectingMessageHandler.class);

    /**
     * Message callback interface for manipulating JMS request messages before sending. 
     */
    public static interface JmsMessageCallback {
        /** Opportunity to decorate generated jms message before forwarding */
        void doWithMessage(javax.jms.Message message, Message<?> request) throws JMSException;
    }

    /**
     * @see com.consol.citrus.message.MessageHandler#handleMessage(org.springframework.integration.Message)
     * @throws CitrusRuntimeException
     */
    public Message<?> handleMessage(final Message<?> request) {
        log.info("Forwarding request to: " + getDestinationName());

        if (log.isDebugEnabled()) {
            log.debug("Message is: " + request.getPayload());
        }
        
        MessageProducer messageProducer = null;
        MessageConsumer messageConsumer = null;
        Destination replyToDestination = null;
        
        Message<?> replyMessage = null;
        try {
            createConnection();
            createSession(connection);
            
            JmsMessageConverter jmsMessageConverter = new JmsMessageConverter(messageConverter, headerMapper);
            javax.jms.Message jmsRequest = jmsMessageConverter.toMessage(request, session);
            
            messageProducer = session.createProducer(getDestination(session));

            replyToDestination = getReplyDestination(session, request);
            if (replyToDestination instanceof TemporaryQueue || replyToDestination instanceof TemporaryTopic) {
                messageConsumer = session.createConsumer(replyToDestination);
            }
            
            jmsRequest.setJMSReplyTo(replyToDestination);
            
            if (messageCallback != null) {
                messageCallback.doWithMessage(jmsRequest, request);
            }
            
            messageProducer.send(jmsRequest);
            
            if (messageConsumer == null) {
                String messageId = jmsRequest.getJMSMessageID().replaceAll("'", "''");
                String messageSelector = "JMSCorrelationID = '" + messageId + "'";
                messageConsumer = session.createConsumer(replyToDestination, messageSelector);
            }
            
            javax.jms.Message jmsReplyMessage = (this.replyTimeout >= 0) ? messageConsumer.receive(replyTimeout) : messageConsumer.receive();
            
            if (jmsReplyMessage != null) {
                replyMessage = (Message<?>)jmsMessageConverter.fromMessage(jmsReplyMessage);
            } else if (fallbackMessageHandlerDelegate != null) {
                log.info("Did not receive reply message from destination '"
                        + getReplyDestination(session, request)
                        + "' - delegating to fallback message handler for response generation");
                
                replyMessage = fallbackMessageHandlerDelegate.handleMessage(request);
            } else {
                log.info("Did not receive reply message from destination '"
                        + getReplyDestination(session, request)
                        + "' - no response is simulated");
                
                replyMessage = null;
            }
        } catch (JMSException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            JmsUtils.closeMessageProducer(messageProducer);
            JmsUtils.closeMessageConsumer(messageConsumer);
            deleteTemporaryDestination(replyToDestination);
        }
        
        return replyMessage;
    }
    
    /**
     * Deletes a temporary destination if present.
     * @param destination
     */
    private void deleteTemporaryDestination(Destination destination) {
        try {
            if (destination instanceof TemporaryQueue) { 
                ((TemporaryQueue) destination).delete();
            } else if (destination instanceof TemporaryTopic) {
                ((TemporaryTopic) destination).delete();
            }
        } catch (JMSException e) {
            log.error("Error while deleting temporary destination", e);
        }
    }

    /**
     * Get the reply destination either as injected instance or from destination name
     * resolver or as temporary reply queue.
     * 
     * @param session
     * @param message
     * @return
     * @throws JMSException
     */
    private Destination getReplyDestination(Session session, Message<?> message) throws JMSException {
        if (message.getHeaders().getReplyChannel() != null) {
            if (message.getHeaders().getReplyChannel() instanceof Destination) {
                return (Destination)message.getHeaders().getReplyChannel();
            } else {
                return new DynamicDestinationResolver().resolveDestinationName(session, 
                                message.getHeaders().getReplyChannel().toString(), 
                                false);
            }
        } else if (replyDestination != null) {
            return replyDestination;
        } else if (StringUtils.hasText(replyDestinationName)) {
            return new DynamicDestinationResolver().resolveDestinationName(session, this.replyDestinationName, false);
        }
        
        return session.createTemporaryQueue();
    }

    /**
     * Get the destination to forward incoming requests to.
     * @param session
     * @return
     * @throws JMSException
     */
    private Destination getDestination(Session session) throws JMSException {
        if (destination != null) {
            return destination;
        }
        
        return new DynamicDestinationResolver().resolveDestinationName(session, destinationName, false);
    }
    
    /**
     * Get the forwarding destination name.
     * @return the destinationName
     */
    public String getDestinationName() {
        try {
            if (destination != null) {
                if (destination instanceof Queue) {
                    return ((Queue)destination).getQueueName();
                } else if (destination instanceof Topic) {
                    return ((Topic)destination).getTopicName();
                } else {
                    return destination.toString();
                }
            } else {
                return destinationName;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }
    
    /**
     * Creates a new JMS connection.
     * @return
     * @throws JMSException
     */
    protected Connection createConnection() throws JMSException {
        if (connection == null) {
            if (connectionFactory instanceof QueueConnectionFactory) {
                this.connection = ((QueueConnectionFactory) connectionFactory).createQueueConnection();
            } else {
                this.connection = connectionFactory.createConnection();
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Created new JMS connection [" + connection + "]");
            }
        }
        
        return connection;
    }
    
    /**
     * Creates a new JMS session.
     * @param connection
     * @return
     * @throws JMSException
     */
    protected Session createSession(Connection connection) throws JMSException {
        if (session == null) {
            if (connection instanceof QueueConnection) {
                session = ((QueueConnection) connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            } else {
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Created new JMS session [" + session + "]");
            }
            
            return session;
        }
        
        return session;
    }
    
    /**
     * Destroy method closing JMS session and connection
     */
    public void destroy() throws Exception {
        JmsUtils.closeSession(session);
        
        if (connection != null) {
            ConnectionFactoryUtils.releaseConnection(connection, this.connectionFactory, true);
        }
    }
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (connectionFactory == null) {
            throw new BeanCreationException("Missing required connectionFactory for JMS message broker connection!");
        }
        
        createConnection();
        createSession(connection);
        
        connection.start();
    }

    /**
     * Gets the jms connection factory.
     * @return
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Set the JMS connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Set the forwarding send destination name.
     * @param sendDestination
     */
    public void setSendDestination(String sendDestination) {
        this.destinationName = sendDestination;
    }

    /**
     * Gets the jms destination.
     * @return
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Set the forwarding destination.
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * Get the destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Gets the jms reply destination.
     * @return
     */
    public Destination getReplyDestination() {
        return replyDestination;
    }

    /**
     * Set the reply destination.
     * @param replyDestination the replyDestination to set
     */
    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    /**
     * Gets the jms reply destination name.
     * @return
     */
    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    /**
     * Set the reply destination name.
     * @param replyDestinationName the replyDestinationName to set
     */
    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }

    /**
     * Gets the reply timeout.
     * @return
     */
    public long getReplyTimeout() {
        return replyTimeout;
    }

    /**
     * Set the reply timeout.
     * @param replyTimeout the replyTimeout to set
     */
    public void setReplyTimeout(long replyTimeout) {
        this.replyTimeout = replyTimeout;
    }

    /**
     * Set the message callback.
     * @param messageCallback the messageCallback to set
     */
    public void setMessageCallback(JmsMessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    /**
     * Gets the fallback message handler.
     * @return
     */
    public MessageHandler getFallbackMessageHandlerDelegate() {
        return fallbackMessageHandlerDelegate;
    }

    /**
     * Set the fallback message handler.
     * @param fallbackMessageHandlerDelegate the fallbackMessageHandlerDelegate to set
     */
    public void setFallbackMessageHandlerDelegate(MessageHandler fallbackMessageHandlerDelegate) {
        this.fallbackMessageHandlerDelegate = fallbackMessageHandlerDelegate;
    }
}
