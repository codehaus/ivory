package org.codehaus.ivory.serialize;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.EnumSerializer;
import org.apache.axis.utils.BeanPropertyDescriptor;

/**
 * DeserializerFactory which uses MetaBeanUtils.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 21, 2003
 */
public class MetadataDeserializerFactory extends BeanDeserializerFactory
{

    public MetadataDeserializerFactory(Class javaType, QName xmlType)
    {
        super(javaType, xmlType);
    }

    /**
      * Get a list of the bean properties
      */
    public static Map getProperties(Class javaType, TypeDesc typeDesc)
    {
        Map propertyMap = null;

        if (typeDesc != null)
        {
            propertyMap = typeDesc.getPropertyDescriptorMap();
        }
        else
        {
            BeanPropertyDescriptor[] pd = MetaBeanUtils.getPd(javaType, null);
            propertyMap = new HashMap();
            // loop through properties and grab the names for later
            for (int i = 0; i < pd.length; i++)
            {
                BeanPropertyDescriptor descriptor = pd[i];
                propertyMap.put(descriptor.getName(), descriptor);
            }
        }

        return propertyMap;
    }
    /**
     * Optimize construction of a BeanDeserializer by caching the
     * type descriptor and property map.
     */
    protected Deserializer getGeneralPurpose(String mechanismType)
    {
        if (javaType == null || xmlType == null)
        {
            return super.getGeneralPurpose(mechanismType);
        }

        if (deserClass == EnumSerializer.class)
        {
            return super.getGeneralPurpose(mechanismType);
        }

        return new MetadataDeserializer(
            javaType,
            xmlType,
            typeDesc,
            propertyMap);
    }

}
