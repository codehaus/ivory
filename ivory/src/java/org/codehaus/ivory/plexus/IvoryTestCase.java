package org.codehaus.ivory.plexus;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.axis.MessageContext;
import org.apache.axis.server.AxisServer;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.transport.local.LocalTransport;
import org.apache.axis.utils.XMLUtils;
import org.codehaus.ivory.AxisService;
import org.codehaus.plexus.PlexusTestCase;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * A generic test-case for testing Ivory and other SOAP services for Plexus.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 4, 2003
 */
public class IvoryTestCase extends PlexusTestCase
{
    private ServletRunner sr;
    
    private ServiceManager manager;
    
    private String services = "http://localhost/services/";
    
    private boolean verbose = false;
    
    public final static String VERBOSE_KEY = 
    	"IvoryTestCase.verbose";

    /** Namespaces for the XPath expressions. */
    private Map namespaces = new HashMap();
    
    public IvoryTestCase(String name)
    {
        super(name);
    }
    
    public void setUp() throws Exception
    {
        super.setUp();
        setVerbose( Boolean.getBoolean( VERBOSE_KEY ) );
    
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        
        InputStream is =
            getClass().getResourceAsStream(
                "/org/codehaus/ivory/plexus/web.xml");
    
        sr = new ServletRunner(is);
    }
    
    protected ServletUnitClient newClient() throws Exception
    {
        return sr.newClient();
    }
    
    /**
     * @return
     */
    public boolean isVerbose()
    {
    	return verbose;
    }
    
    /**
     * @param b
     */
    public void setVerbose(boolean b)
    {
    	verbose = b;
    }
    
    /**
     * Assert that the response contains a string.
     * @param response
     * @param searchfor
     * @throws IOException
     */
    public void assertStringInBody(
    	String body,
    	String searchfor)
    	throws IOException
    {
    	boolean found = body.indexOf(searchfor) >= 0;
    	if (!found)
    	{
    		String message;
    		message = "failed to find [" + searchfor + "].\n";
    		
    		if ( isVerbose() )
    			 message += "Body:\n" + body;
    			 
    		fail(message);
    	}
    }
    
    /**
     * Assert that the response contains a string.
     * @param response
     * @param searchfor
     * @throws IOException
     */
    public void assertStringInBody(
    	WebResponse response,
    	String searchfor)
    	throws IOException
    {
    	String body = response.getText();
    	boolean found = body.indexOf(searchfor) >= 0;
    	if (!found)
    	{
    		String message;
    		message = "failed to find [" + searchfor + "]";
    		
    		if ( isVerbose() )
    			 message += "Body:\n" + body;
    			 
    		fail(message);
    	}
    }
    
    /**
     * Assert that the response contains a string.
     * @param response
     * @param searchfor
     * @param url
     * @throws IOException
     */
    public void assertStringInBody(
        WebResponse response,
        String searchfor,
        String url)
        throws IOException
    {
        String body = response.getText();
        boolean found = body.indexOf(searchfor) >= 0;
        if (!found)
        {
            String message;
            message = "failed to find [" + searchfor + "] at " + url;
    		
    		if ( isVerbose() )
    			 message += "Body:\n" + body;
    			 
            fail(message);
        }
    }
    
    /**
     * Assert that a named string is in the request body of the.
     * 
     * response to a request
     * @param request what we ask
     * @param searchfor string to look for
     * @throws IOException when the fetch fails
     * @throws org.xml.sax.SAXException
     */
    protected void assertStringInBody(WebRequest request, String searchfor)
        throws IOException, org.xml.sax.SAXException
    {
        WebResponse response = makeRequest(request);
        assertStringInBody(response, searchfor, request.getURL().toString());
    }
    
    /**
     * Make a request in a new session.
     * @param request   request to make
     * @return the response
     * @throws IOException
     * @throws SAXException
     */
    protected WebResponse makeRequest(WebRequest request)
        throws IOException, SAXException
    {
        WebConversation session = new WebConversation();
        WebResponse response = session.getResponse(request);
        return response;
    }
    
    /**
     * Assert that a string is not in a response.
     * @param response
     * @param searchfor
     * @param url
     * @throws IOException
     */
    protected void assertStringNotInBody(
    	String body,
    	String searchfor)
    	throws IOException
    {
    	boolean found = body.indexOf(searchfor) >= 0;
    	if (found)
    	{
    		String message;
    		message = "unexpectedly found [" + searchfor + "].";
    		
    		if ( isVerbose() )
    			 message += "Body:\n" + body;
    			 
    		fail(message);
    	}
    }
    
    /**
     * Assert that a string is not in a response.
     * @param response
     * @param searchfor
     * @param url
     * @throws IOException
     */
    protected void assertStringNotInBody(
    	WebResponse response,
    	String searchfor)
    	throws IOException
    {
    	String body = response.getText();
    	boolean found = body.indexOf(searchfor) >= 0;
    	if (found)
    	{
    		String message;
    		message = "unexpectedly found [" + searchfor + "].";
    		
    		if ( isVerbose() )
    			 message += "Body:\n" + body;
    			 
    		fail(message);
    	}
    }
    
    /**
     * Assert that a string is not in a response.
     * @param response
     * @param searchfor
     * @param url
     * @throws IOException
     */
    protected void assertStringNotInBody(
        WebResponse response,
        String searchfor,
        String url)
        throws IOException
    {
        String body = response.getText();
        boolean found = body.indexOf(searchfor) >= 0;
        if (found)
        {
            String message;
            message = "unexpectedly found [" + searchfor + "] at " + url;
    		
    		if ( isVerbose() )
    			 message += "Body:\n" + body;
    			 
            fail(message);
        }
    
    }
    
    /**
     * Assert that a string is not in the response to a request.
     * @param request
     * @param searchfor
     * @throws IOException
     * @throws org.xml.sax.SAXException
     */
    protected void assertStringNotInBody(WebRequest request, String searchfor)
        throws IOException, org.xml.sax.SAXException
    {
        WebConversation session = new WebConversation();
        WebResponse response = session.getResponse(request);
        assertStringNotInBody(response, searchfor, request.getURL().toString());
    }
    
    protected void assertIsXml(String response)
    {
        if(  response.indexOf("<?xml") != 0 )
        {
        	fail( "Invalid XML:\n" + response );
        } 
    }
    
    /**
     * Here we expect an errorCode other than 200, and look for it
     * checking for text is omitted as it doesnt work. It would never work on
     * java1.3, but one may have expected java1.4+ to have access to the
     * error stream in responses. Clearly not.
     * @param request
     * @param errorCode
     * @param errorText optional text string to search for
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     */
    protected void expectErrorCode(
        WebRequest request,
        int errorCode,
        String errorText)
        throws MalformedURLException, IOException, SAXException
    {
        WebConversation session = new WebConversation();
        String failureText =
            "Expected error " + errorCode + " from " + request.getURL();
    
        try
        {
            session.getResponse(request);
            fail(errorText + " -got success instead");
        }
        catch (HttpException e)
        {
            assertEquals(failureText, errorCode, e.getResponseCode());
            /* checking for text omitted as it doesnt work.
            if(errorText!=null) {
            	assertTrue(
            			"Failed to find "+errorText+" in "+ e.getResponseMessage(),
            			e.getMessage().indexOf(errorText)>=0);
            }
            */
        }
    }
    
    /**
     * Verifies that the service generates WSDL.
     * 
     * @param service
     * @param method
     */
    public void assertValidWSDL( String serviceName, String method )
    	throws Exception
    {
    	assertValidWSDL( serviceName, new String[]{ method } );
    }
    
    public String getWSDL( String serviceName ) throws Exception
    {
    	AxisService service = ( AxisService ) lookup( AxisService.ROLE );
    	AxisServer server = service.getAxisServer();
    
    	LocalTransport transport = new LocalTransport(server);
    
    	MessageContext msgContext = new MessageContext(server);
    	msgContext.setSOAPConstants(SOAPConstants.SOAP12_CONSTANTS);
    	msgContext.setEncodingStyle(SOAPConstants.SOAP12_CONSTANTS.getEncodingURI());
    
    	msgContext.setTargetService( serviceName );
        
    	// During a real invocation this is set by the handler, however we
    	// need to set it hear to get the wsdl generation working.
    	msgContext.setProperty( MessageContext.TRANS_URL, 
    							services + serviceName );
    	server.generateWSDL( msgContext );        
        
    	// another one of those undocumented "features"
    	Document doc = (Document) msgContext.getProperty( "WSDL" );
        
    	StringWriter writer = new StringWriter();
    	XMLUtils.DocumentToWriter(doc, writer);
    	
    	return writer.toString();
    }
    
    /**
     * Verifies that the service generates WSDL.
     * 
     * @param service
     * @param methods
     */
    public void assertValidWSDL( String serviceName, String methods[] )
        throws Exception
    {
        String response = getWSDL( serviceName );
    
    	if ( isVerbose() )
    	{
    		System.out.println( "WSDL for " + serviceName + ":" );
    		System.out.println( response );
    	}
    	
    	assertIsXml( response );
    	
    	for ( int i = 0; i < methods.length; i++ )
    	{
    		assertStringInBody( response, "<wsdl:operation name=\"" + methods[i] + "\">" );
    		assertStringInBody( response, "<wsdl:input name=\"" + methods[i] + "Request\">" );
    		assertStringInBody( response, "<wsdl:output name=\"" + methods[i] + "Response\">" );
    	}
    }
    
    /**
     * Assert that the following XPath query selects one or more nodes.
     * 
     * @param xpath
     * @throws Exception
     */
    public void assertValid( String xpath, Node node )
        throws Exception
    {
        List nodes = createXPath( xpath ).selectNodes( node );
        
        if ( nodes.size() == 0 )
        {
            throw new Exception( "Failed to select any nodes for expression:.\n" +
                                 xpath + "\n" +
                                 node.asXML() );
        }
    }
    
    /**
     * Assert that the following XPath query selects no nodes.
     * 
     * @param xpath
     * @throws Exception
     */
    public void assertInvalid( String xpath, Node node )
        throws Exception
    {
        List nodes = createXPath( xpath ).selectNodes( node );
        
        if ( nodes.size() > 0 )
        {
            throw new Exception( "Found multiple nodes for expression:\n" +
                                 xpath + "\n" +
                                 node.asXML() );
        }
    }

    /**
     * Asser that the text of the xpath node retrieved is equal to the
     * value specified.
     * 
     * @param xpath
     * @param value
     * @param node
     * @throws Exception
     */
    public void assertXPathEquals( String xpath, String value, Node node )
        throws Exception
    {
        Node n = createXPath( xpath ).selectSingleNode( node );
        
        if ( n == null )
            fail("Couldn't select a valid node.");
        
        String value2 = n.getText().trim();
        assertEquals( value, value2 );
    }

    /**
     * Create the specified XPath expression with the namespaces added
     * via addNamespace().
     */
    protected XPath createXPath( String xpathString )
    {
        XPath xpath = DocumentHelper.createXPath( xpathString );
        xpath.setNamespaceURIs(namespaces);
        
        return xpath;
    }

    /**
     * Add a namespace that will be used for XPath expressions.
     * @param ns Namespace name.
     * @param uri The namespace uri.
     */
    public void addNamespace( String ns, String uri )
    {
        namespaces.put(ns, uri);
    }
}
