package org.codehaus.ivory.serialize;

import java.lang.reflect.Method;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.attributes.Attributes;
import org.codehaus.ivory.attributes.NonWebMethod;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 22, 2003
 */
public class MetaSerializerTest extends TestCase
{
    public MetaSerializerTest( String name )
    {
        super(name);
    }
    
    public void testSerializer() throws Exception
    {
        MetadataSerializer serializer = new MetadataSerializer( MetaBean.class,
            new QName( MetaBean.class.getName() ) );
    }
    
    public void testDeserializer() throws Exception
    {
        Map properties =
            MetadataDeserializerFactory.getProperties(MetaBean.class, null);
        
        Method m = MetaBean.class.getMethod("getWorld", new Class[0] );
        assertTrue( Attributes.hasAttributeType(m, NonWebMethod.class) );
        
        assertTrue( properties.containsKey("hello") );
        assertTrue( !properties.containsKey("world") );
    }
}
