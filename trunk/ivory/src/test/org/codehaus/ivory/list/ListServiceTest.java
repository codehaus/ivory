package org.codehaus.ivory.list;

import org.codehaus.ivory.IvoryTestCase;
import org.dom4j.Document;

/**
 * Tests the ability to serialize lists as arrays.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 9, 2003
 */
public class ListServiceTest
    extends IvoryTestCase
{
    public ListServiceTest(String name)
    {
        super(name);
    }
    
    public void testListSerialization() throws Exception
    {
    	addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        
        Document doc = getWSDL("ListService");
        
        assertInvalid("/wsdl:definitions/wsdl:message[@name='secretMethodResponse']", doc );
        assertValid("/wsdl:definitions/wsdl:message[@name='getDevelopersResponse']", doc );
        assertValid("/wsdl:definitions/wsdl:message[@name='getDevelopersResponse']/wsdl:part[@type='impl:ArrayOf_xsd_string']", doc );

     }     
}

