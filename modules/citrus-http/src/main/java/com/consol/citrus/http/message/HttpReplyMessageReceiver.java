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

package com.consol.citrus.http.message;

import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.message.ReplyMessageReceiver;
import org.springframework.integration.Message;

/**
 * @author Christoph Deppisch
 * @since 1.4
 * @deprecated
 */
public class HttpReplyMessageReceiver extends ReplyMessageReceiver {

    /**
     * Default constructor.
     */
    public HttpReplyMessageReceiver() {
        this(new HttpClient());
    }

    /**
     * Default constructor with Jms endpoint.
     * @param endpoint
     */

    public HttpReplyMessageReceiver(HttpClient endpoint) {
        super(endpoint);
    }

    @Override
    public HttpClient getEndpoint() {
        return (HttpClient) super.getEndpoint();
    }

    @Override
    public HttpEndpointConfiguration getEndpointConfiguration() {
        return (HttpEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public Message<?> receive() {
        return getEndpoint().createConsumer().receive("", getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message<?> receive(long timeout) {
        return getEndpoint().createConsumer().receive("", timeout);
    }

    @Override
    public Message<?> receiveSelected(String selector) {
        return getEndpoint().createConsumer().receive(selector, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        return getEndpoint().createConsumer().receive(selector, timeout);
    }

    @Override
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        getEndpoint().onReplyMessage(correlationKey, replyMessage);
    }

    @Override
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
