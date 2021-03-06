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

package com.consol.citrus.channel;

import org.springframework.integration.MessageChannel;

/**
 * Interface defines methods for reply message holders to return message
 * channels for synchronous communication.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public interface ReplyMessageChannelHolder {
    /**
     * Get reply message channel with given correlation key.
     * @param correlationKey
     * @return
     */
    MessageChannel getReplyMessageChannel(String correlationKey);
    
    /**
     * Get reply message channel.
     * @return
     */
    MessageChannel getReplyMessageChannel();
}
