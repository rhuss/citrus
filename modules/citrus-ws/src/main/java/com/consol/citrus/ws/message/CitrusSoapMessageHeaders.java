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

package com.consol.citrus.ws.message;

import com.consol.citrus.message.CitrusMessageHeaders;

/**
 * @author Christoph Deppisch
 */
public abstract class CitrusSoapMessageHeaders {
    
    /**
     * Prevent instantiation.
     */
    private CitrusSoapMessageHeaders() {
    }
    
    /** Citrus ws specific header prefix */
    public static final String SOAP_PREFIX = CitrusMessageHeaders.PREFIX + "soap_";

    /** Special header prefix for http transport headers in SOAP message sender */
    public static final String HTTP_PREFIX = CitrusMessageHeaders.PREFIX + "http_";
    
    /** Special status code header */
    public static final String HTTP_STATUS_CODE = HTTP_PREFIX + "status_code";

    /** Server context path */
    public static final String HTTP_CONTEXT_PATH = HTTP_PREFIX + "context_path";

    /** Full http request uri */
    public static final String HTTP_REQUEST_URI = HTTP_PREFIX + "request_uri";

    /** Http request method */
    public static final String HTTP_REQUEST_METHOD = HTTP_PREFIX + "method";

    /** Http query parameters */
    public static final String HTTP_QUERY_PARAMS = HTTP_PREFIX + "query_params";

    /** SOAP action header name */
    public static final String SOAP_ACTION = SOAP_PREFIX + "action";
    
    /** Soap fault code specific header */
    public static final String SOAP_FAULT = SOAP_PREFIX + "fault";
    
    /** Soap fault detail specific header */
    public static final String SOAP_FAULT_DETAIL = SOAP_PREFIX + "fault_detail";
    
    /** Soap fault detail specific header */
    public static final String SOAP_FAULT_DETAIL_RESOURCE = SOAP_FAULT_DETAIL + "_resource";
    
    /** Soap attachment header prefix */
    public static final String SOAP_ATTACHMENT_PREFIX = SOAP_PREFIX + "attachment_";
    
    /** Content id header name*/
    public static final String CONTENT_ID = SOAP_ATTACHMENT_PREFIX + "contentId";
    
    /** Content type header name*/
    public static final String CONTENT_TYPE = SOAP_ATTACHMENT_PREFIX + "contentType";
    
    /** Content body header name*/
    public static final String CONTENT = SOAP_ATTACHMENT_PREFIX + "content";
    
    /** Charset header name*/
    public static final String CHARSET_NAME = SOAP_ATTACHMENT_PREFIX + "charset";
    
}
