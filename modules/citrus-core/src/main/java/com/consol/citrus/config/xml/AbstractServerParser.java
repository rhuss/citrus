/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.config.xml;

import com.consol.citrus.channel.MessageSelectingQueueChannel;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Abstract server parser adds endpoint adapter construction and basic server property parsing.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractServerParser extends AbstractBeanDefinitionParser {

    @Override
    protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder serverBuilder = BeanDefinitionBuilder.genericBeanDefinition(getServerClass());

        BeanDefinitionParserUtils.setPropertyValue(serverBuilder, element.getAttribute("auto-start"), "autoStart");

        if (element.hasAttribute("endpoint-adapter")) {
            BeanDefinitionParserUtils.setPropertyReference(serverBuilder, element.getAttribute("endpoint-adapter"), "endpointAdapter");
        } else {
            String channelId = element.getAttribute(ID_ATTRIBUTE) + AbstractServer.DEFAULT_CHANNEL_ID_SUFFIX;
            BeanDefinitionParserUtils.registerBean(channelId, MessageSelectingQueueChannel.class, parserContext, shouldFireEvents());
        }

        BeanDefinitionParserUtils.setPropertyReference(serverBuilder, element.getAttribute("interceptors"), "interceptors");

        parseServer(serverBuilder, element, parserContext);

        return serverBuilder.getBeanDefinition();
    }

    /**
     * Parses element and adds server properties to bean definition via provided builder.
     * Subclasses must implement this parsing method in order to add detailed server bean definition properties.
     * @param serverBuilder
     * @param element
     * @param parserContext
     * @return
     */
    protected abstract void parseServer(BeanDefinitionBuilder serverBuilder, Element element, ParserContext parserContext);

    /**
     * Subclasses must provide proper server class implementation.
     * @return
     */
    protected abstract Class<? extends AbstractServer> getServerClass();
}