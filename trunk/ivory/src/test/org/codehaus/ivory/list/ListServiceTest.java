package org.codehaus.ivory.list;

import org.codehaus.ivory.plexus.IvoryTestCase;

import com.meterware.httpunit.WebResponse;

/**
 * Tests the DefaultAxisService.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 9, 2003
 */
public class ListServiceTest extends IvoryTestCase
{
    public ListServiceTest(String name)
    {
        super(name);
    }
    
    public void testListSerialization() throws Exception
    {
    	assertValidWSDL( "List", "getDevelopers" );
    	
    	String wsdl = getWSDL( "List" );
        assertStringNotInBody( "wsdl", "secretMethod" );

		WebResponse response = 
			newClient().getResponse("http://localhost/services/List?method=getDevelopers");
		
		assertIsXml( response.getText() );
		assertStringInBody( response, "Dan Diephouse" );
		assertStringInBody( response, "Jason van Zyl" );

     }        
}

