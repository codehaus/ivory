package org.codehaus.ivory.serialize;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.axis.AxisFault;
import org.apache.axis.InternalException;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.utils.BeanPropertyDescriptor;
import org.apache.axis.utils.BeanUtils;
import org.apache.commons.attributes.Attributes;
import org.apache.commons.logging.Log;
import org.apache.axis.components.logger.LogFactory;
import org.codehaus.ivory.attributes.NonWebMethod;

/**
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 21, 2003
 */
public class MetaBeanUtils extends BeanUtils
{

    public static final Object[] noArgs = new Object[] {
    };
    protected static Log log = LogFactory.getLog(BeanUtils.class.getName());

    /**
     * Create a BeanPropertyDescriptor array for the indicated class.
     * @param javaType
     * @return an ordered array of properties
     */
    public static BeanPropertyDescriptor[] getPd(Class javaType)
    {
        return MetaBeanUtils.getPd(javaType, null);
    }

    /**
     * Create a BeanPropertyDescriptor array for the indicated class.
     * @param javaType
     * @param typeDesc
     * @return an ordered array of properties
     */
    public static BeanPropertyDescriptor[] getPd(
        Class javaType,
        TypeDesc typeDesc)
    {
        BeanPropertyDescriptor[] pd;
        try
        {
            final Class secJavaType = javaType;

            // Need doPrivileged access to do introspection.
            PropertyDescriptor[] rawPd = MetaBeanUtils.getPropertyDescriptors(secJavaType);
            pd = MetaBeanUtils.processPropertyDescriptors(rawPd, javaType, typeDesc);
        }
        catch (Exception e)
        {
            // this should never happen
            throw new InternalException(e);
        }
        return pd;
    }

    private static PropertyDescriptor[] getPropertyDescriptors(final Class secJavaType)
    {
        return (
            PropertyDescriptor[]) AccessController
                .doPrivileged(new PrivilegedAction()
        {
            public Object run()
            {
                PropertyDescriptor[] result = null;
                // START FIX http://nagoya.apache.org/bugzilla/showattachment.cgi?attach_id=4937
                try
                {
                    // privileged code goes here
                    if (AxisFault.class.isAssignableFrom(secJavaType))
                    {
                        // Don't include AxisFault data
                        result =
                            Introspector
                                .getBeanInfo(secJavaType, AxisFault.class)
                                .getPropertyDescriptors();
                    }
                    else if (Throwable.class != secJavaType && Throwable.class.isAssignableFrom(secJavaType))
                    {
                        // Don't include Throwable data
                        result =
                            Introspector
                                .getBeanInfo(secJavaType, Throwable.class)
                                .getPropertyDescriptors();
                    }
                    else
                    {
                        // privileged code goes here
                        result =
                            Introspector
                                .getBeanInfo(secJavaType)
                                .getPropertyDescriptors();
                    }
                    // END FIX http://nagoya.apache.org/bugzilla/showattachment.cgi?attach_id=4937
                }
                catch (java.beans.IntrospectionException Iie)
                {
                }
                return result;
            }
        });
    }

    /**
     * Return a list of properties in the bean which should be attributes
     */
    public static Vector getBeanAttributes(Class javaType, TypeDesc typeDesc)
    {
        Vector ret = new Vector();

        if (typeDesc == null)
        {
            // !!! Support old-style beanAttributeNames for now

            // See if this object defined the 'getAttributeElements' function
            // which returns a Vector of property names that are attributes
            try
            {
                Method getAttributeElements =
                    javaType.getMethod("getAttributeElements", new Class[] {
                });
                // get string array
                String[] array =
                    (String[]) getAttributeElements.invoke(null, noArgs);

                // convert it to a Vector
                ret = new Vector(array.length);
                for (int i = 0; i < array.length; i++)
                {
                    ret.add(array[i]);
                }
            }
            catch (Exception e)
            {
                ret.clear();
            }
        }
        else
        {
            FieldDesc[] fields = typeDesc.getFields();
            if (fields != null)
            {
                for (int i = 0; i < fields.length; i++)
                {
                    FieldDesc field = fields[i];
                    if (!field.isElement())
                    {
                        ret.add(field.getFieldName());
                    }
                }
            }
        }

        return ret;
    }
    /**
     * This method attempts to sort the property descriptors using
     * the typeDesc and order defined in the class.
     *
     * This routine also looks for set(i, type) and get(i) methods and adjusts the
     * property to use these methods instead.  These methods are generated by the
     * emitter for "collection" of properties (i.e. maxOccurs="unbounded" on an element).
     * JAX-RPC is silent on this issue, but web services depend on this kind of behaviour.
     * The method signatures were chosen to match bean indexed properties.
     */
    public static BeanPropertyDescriptor[] processPropertyDescriptors(
        PropertyDescriptor[] rawPd,
        Class cls)
    {
        return MetaBeanUtils.processPropertyDescriptors(rawPd, cls, null);
    }

    public static BeanPropertyDescriptor[] processPropertyDescriptors(
        PropertyDescriptor[] rawPd,
        Class cls,
        TypeDesc typeDesc)
    {
        ArrayList filteredPds = new ArrayList();

        for (int i = 0; i < rawPd.length; i++)
        {
            Method m = rawPd[i].getReadMethod();

            if (!Attributes.hasAttributeType(m, NonWebMethod.class))
            {
                filteredPds.add(rawPd[i]);
            }
            else
            {
                // TODO: How should we log this?
                System.out.println("[ivory] skipped method " + m.getName());
            }
        }

        return BeanUtils.processPropertyDescriptors(
            (PropertyDescriptor[]) filteredPds.toArray( new PropertyDescriptor[0] ),
            cls,
            typeDesc);
    }
}
