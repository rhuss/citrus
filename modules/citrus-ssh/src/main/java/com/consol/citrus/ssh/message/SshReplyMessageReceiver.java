/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.ssh.message;

import com.consol.citrus.message.ReplyMessageReceiver;
import com.consol.citrus.ssh.client.SshClient;
import com.consol.citrus.ssh.client.SshEndpointConfiguration;
import org.springframework.integration.Message;

/**
 * @author Christoph Deppisch
 * @since 1.4
 * @deprecated
 */
public class SshReplyMessageReceiver extends ReplyMessageReceiver {

    /**
     * Default constructor.
     */
    public SshReplyMessageReceiver() {
        this(new SshClient());
    }

    /**
     * Default constructor with Jms endpoint.
     * @param endpoint
     */

    public SshReplyMessageReceiver(SshClient endpoint) {
        super(endpoint);
    }

    @Override
    public SshClient getEndpoint() {
        return (SshClient) super.getEndpoint();
    }

    @Override
    public SshEndpointConfiguration getEndpointConfiguration() {
        return (SshEndpointConfiguration) super.getEndpointConfiguration();
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return getEndpoint().createConsumer().receive("", getEndpointConfiguration().getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return getEndpoint().createConsumer().receive("", timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return getEndpoint().createConsumer().receive(selector, getEndpointConfiguration().getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        return getEndpoint().createConsumer().receive(selector, timeout);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message, java.lang.String)
     */
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        getEndpoint().onReplyMessage(correlationKey, replyMessage);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message)
     */
    public void onReplyMessage(Message<?> replyMessage) {
        getEndpoint().onReplyMessage("", replyMessage);
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return getEndpointConfiguration().getPollingInterval();
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        getEndpointConfiguration().setPollingInterval(pollingInterval);
    }
}