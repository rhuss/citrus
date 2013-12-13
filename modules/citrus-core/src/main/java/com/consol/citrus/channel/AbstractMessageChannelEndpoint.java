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

package com.consol.citrus.channel;

import com.consol.citrus.endpoint.AbstractMessageEndpoint;
import com.consol.citrus.endpoint.SelectionEndpoint;
import org.springframework.integration.Message;

/**
 * Abstract message channel endpoint adapts to selector receive variant. By default setting null selector.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractMessageChannelEndpoint extends AbstractMessageEndpoint implements SelectionEndpoint {

    @Override
    public Message<?> receive(long timeout) {
        return receive(null, timeout);
    }
}