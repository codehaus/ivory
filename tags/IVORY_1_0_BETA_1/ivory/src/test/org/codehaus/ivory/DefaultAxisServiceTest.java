package org.codehaus.ivory;


import org.dom4j.Document;

import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;

/**
 * Tests the DefaultAxisService.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 9, 2003
 */
public class DefaultAxisServiceTest
	extends IvoryTestCase
{
    public DefaultAxisServiceTest(String name)
    {
        super(name);
    }
    
    public void testAxisService() throws Exception
    {
    	AxisService service = ( AxisService ) lookup( AxisService.ROLE );
    	
    	assertNotNull( service.getAxisServer() );
    }
    
	public void testWSDLGeneration() throws Exception
	{
        addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        
        Document doc = getWSDL("Version");
        
        assertValid("/wsdl:definitions/wsdl:message[@name='getVersionResponse']", doc );
	}

    public void testExposeService() throws Exception
    {
        ServletUnitClient client = newClient();

		WebResponse response = client.getResponse("http://localhost/services/SparePartInfo?method=getPartInfo&PartSKU=test");

		//assertIsXml( response.getText() );
		//assertStringInBody( response, "<getPartInfoReturn" );
		//assertStringInBody( response, "test - Part Info" );
    }
    
    public void testComplexSerialization() throws Exception
    {
        ServletUnitClient client = newClient();

		WebResponse response = client.getResponse("http://localhost/services/SparePartInfo?method=getSparePart");
		
		//assertIsXml( response.getText() );
        //assertStringInBody( response, "<SKU" );
    }
}

