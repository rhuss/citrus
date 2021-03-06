/*
 * Copyright 2006-2012 the original author or authors.
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

import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bean definition parser for purge-channel action in test case.
 * 
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeMessageChannelAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        if (element.hasAttribute("message-selector")) {
            BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("message-selector"), "messageSelector");
        }
        
        List<String> channelNames = new ArrayList<String>();
        ManagedList channelRefs = new ManagedList();
        List<?> channelElements = DomUtils.getChildElementsByTagName(element, "channel");
        for (Iterator<?> iter = channelElements.iterator(); iter.hasNext();) {
            Element channel = (Element) iter.next();
            String channelName = channel.getAttribute("name");
            String channelRef = channel.getAttribute("ref");
            
            if (StringUtils.hasText(channelName)) {
                channelNames.add(channelName);
            } else if (StringUtils.hasText(channelRef)) {
                channelRefs.add(BeanDefinitionBuilder.childBeanDefinition(channelRef).getBeanDefinition());
            } else {
                throw new BeanCreationException("Element 'channel' must set one of the attributes 'name' or 'ref'");
            }
        }
        
        beanDefinition.addPropertyValue("channelNames", channelNames);
        beanDefinition.addPropertyValue("channels", channelRefs);

        return beanDefinition.getBeanDefinition();
    }
}
