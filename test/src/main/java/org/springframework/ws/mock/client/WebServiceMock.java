/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringSource;

/**
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public abstract class WebServiceMock {

    public static void mockWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        Assert.notNull(webServiceTemplate, "'webServiceTemplate' must not be null");

        MockWebServiceMessageSender mockMessageSender = new MockWebServiceMessageSender();
        webServiceTemplate.setMessageSender(mockMessageSender);
        MockWebServiceMessageSenderHolder.set(mockMessageSender);
    }

    /**
     * Records an expectation specified by the given {@link RequestMatcher}. Returns a {@link ResponseActions} object
     * that allows for setting up the response, or more expectations.
     *
     * @param requestMatcher the request matcher expected
     * @return the response actions
     */
    public static ResponseActions expect(RequestMatcher requestMatcher) {
        MockWebServiceMessageSender messageSender = MockWebServiceMessageSenderHolder.get();
        Assert.state(messageSender != null,
                "WebServiceTemplate has not been mocked. Did you call mockWebServiceTemplate() ?");

        MockSenderConnection connection = messageSender.expectNewConnection();
        connection.addRequestMatcher(requestMatcher);
        return connection;
    }

    // RequestMatchers

    /**
     * Expects the given String XML payload.
     *
     * @param payload the XML payload
     * @return the request matcher
     */
    public static RequestMatcher payload(String payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadMatcher(new StringSource(payload));
    }

    /**
     * Expects the given {@link Source} XML payload.
     *
     * @param payload the XML payload
     * @return the request matcher
     */
    public static RequestMatcher payload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadMatcher(payload);
    }

    /**
     * Expects the given {@link Resource} XML payload.
     *
     * @param payload the XML payload
     * @return the request matcher
     */
    public static RequestMatcher payload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadMatcher(createResourceSource(payload));
    }

    /**
     * Expects the given SOAP header in the outgoing message.
     *
     * @param soapHeaderName the qualified name of the SOAP header to expect
     * @return the request matcher
     */
    public static RequestMatcher soapHeader(QName soapHeaderName) {
        Assert.notNull(soapHeaderName, "'soapHeaderName' must not be null");
        return new SoapHeaderMatcher(soapHeaderName);
    }

    /**
     * Expects a connection to the given URI.
     *
     * @param uri the String uri expected to connect to
     * @return the request matcher
     */
    public static RequestMatcher connectionTo(String uri) {
        Assert.notNull(uri, "'uri' must not be null");
        return connectionTo(URI.create(uri));
    }

    /**
     * Expects a connection to the given URI.
     *
     * @param uri the String uri expected to connect to
     * @return the request matcher
     */
    public static RequestMatcher connectionTo(URI uri) {
        Assert.notNull(uri, "'uri' must not be null");
        return new UriMatcher(uri);
    }

    // ResponseCallbacks

    /**
     * Respond with the given String XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCallback withPayload(String payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadResponseCallback(new StringSource(payload));
    }

    /**
     * Respond with the given {@link Source} XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCallback withPayload(Source payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadResponseCallback(payload);
    }

    /**
     * Respond with the given {@link Resource} XML as payload response.
     *
     * @param payload the response payload
     * @return the response callback
     */
    public static ResponseCallback withPayload(Resource payload) {
        Assert.notNull(payload, "'payload' must not be null");
        return new PayloadResponseCallback(createResourceSource(payload));
    }

    /**
     * Respond with an error.
     *
     * @param errorMessage the error message
     * @return the response callback
     * @see org.springframework.ws.transport.WebServiceConnection#hasError()
     * @see org.springframework.ws.transport.WebServiceConnection#getErrorMessage()
     */
    public static ResponseCallback withError(String errorMessage) {
        Assert.hasLength(errorMessage, "'errorMessage' must not be empty");
        return new ErrorResponseCallback(errorMessage);
    }

    /**
     * Respond with an {@link IOException}.
     *
     * @param ioException the exception to be thrown
     * @return the response callback
     */
    public static ResponseCallback withException(IOException ioException) {
        Assert.notNull(ioException, "'ioException' must not be null");
        return new ExceptionResponseCallback(ioException);
    }

    /**
     * Respond with an {@link RuntimeException}.
     *
     * @param ex the runtime exception to be thrown
     * @return the response callback
     */
    public static ResponseCallback withException(RuntimeException ex) {
        Assert.notNull(ex, "'ex' must not be null");
        return new ExceptionResponseCallback(ex);
    }

    /**
     * Respond with a {@code MustUnderstand} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addMustUnderstandFault(String, Locale)
     */
    public static ResponseCallback withMustUnderstandFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCallback.createMustUnderstandFault(faultStringOrReason, locale);
    }
    /**
     * Respond with a {@code Client} (SOAP 1.1) or {@code Sender} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseCallback withClientOrSenderFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCallback.createClientOrSenderFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code Server} (SOAP 1.1) or {@code Receiver} (SOAP 1.2) fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String, Locale)
     */
    public static ResponseCallback withServerOrReceiverFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCallback.createServerOrReceiverFault(faultStringOrReason, locale);
    }

    /**
     * Respond with a {@code VersionMismatch} fault.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
     * @param locale              the language of faultStringOrReason. Optional for SOAP 1.1
     * @see org.springframework.ws.soap.SoapBody#addVersionMismatchFault(String, Locale)
     */
    public static ResponseCallback withVersionMismatchFault(String faultStringOrReason, Locale locale) {
        Assert.hasLength(faultStringOrReason, "'faultStringOrReason' must not be empty");
        return SoapFaultResponseCallback.createVersionMismatchFault(faultStringOrReason, locale);
    }


    private static ResourceSource createResourceSource(Resource resource) {
        try {
            return new ResourceSource(resource);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException(resource + " could not be opened", ex);
        }
    }


}