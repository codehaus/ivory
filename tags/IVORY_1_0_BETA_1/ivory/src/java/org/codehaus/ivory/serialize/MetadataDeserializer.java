package org.codehaus.ivory.serialize;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.ser.BeanDeserializer;

/**
 * A BeanDeserializer with metadata support.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 21, 2003
 */
public class MetadataDeserializer
    extends BeanDeserializer
{
    // Construct BeanSerializer for the indicated class/qname
    public MetadataDeserializer(Class javaType, QName xmlType)
    {
        this(javaType, xmlType, TypeDesc.getTypeDescForClass(javaType));
    }

    // Construct BeanDeserializer for the indicated class/qname and meta Data
    public MetadataDeserializer(
        Class javaType,
        QName xmlType,
        TypeDesc typeDesc)
    {
        this(
            javaType,
            xmlType,
            typeDesc,
            MetadataDeserializerFactory.getProperties(javaType, typeDesc));
    }

    // Construct BeanDeserializer for the indicated class/qname and meta Data
    public MetadataDeserializer(
        Class javaType,
        QName xmlType,
        TypeDesc typeDesc,
        Map propertyMap)
    {
        super(javaType, xmlType, typeDesc, propertyMap);
    }
}
