package org.codehaus.ivory.plexus;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.axis.MessageContext;
import org.apache.axis.server.AxisServer;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.transport.local.LocalTransport;
import org.codehaus.ivory.AxisService;
import org.codehaus.plexus.PlexusTestCase;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
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
    
    public org.dom4j.Document getWSDL( String serviceName ) throws Exception
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
        
        DOMReader xmlReader = new DOMReader();
        return xmlReader.read(doc);
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
