package org.codehaus.ivory.serialize;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.axis.wsdl.fromJava.Types;
import javax.xml.namespace.QName;

import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.ser.BeanSerializer;
import org.apache.axis.utils.BeanPropertyDescriptor;
import org.w3c.dom.Element;

/**
 * A BeanSerializer with metadata support.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 21, 2003
 */
public class MetadataSerializer extends BeanSerializer
{
    // Construct BeanSerializer for the indicated class/qname
    public MetadataSerializer(Class javaType, QName xmlType)
    {
        this(javaType, xmlType, TypeDesc.getTypeDescForClass(javaType));
    }

    // Construct BeanSerializer for the indicated class/qname
    public MetadataSerializer(Class javaType, QName xmlType, TypeDesc typeDesc)
    {
        super(javaType, xmlType, typeDesc, null);

        if (typeDesc != null)
        {
            propertyDescriptor = typeDesc.getPropertyDescriptors();
        }
        else
        {
            propertyDescriptor = MetaBeanUtils.getPd(javaType, null);
        }
    }

    // Construct BeanSerializer for the indicated class/qname/propertyDesc
    public MetadataSerializer(
        Class javaType,
        QName xmlType,
        TypeDesc typeDesc,
        BeanPropertyDescriptor[] propertyDescriptor)
    {
        super(javaType, xmlType, typeDesc, propertyDescriptor);
    }

    /**
     * Return XML schema for the specified type, suitable for insertion into
     * the &lt;types&gt; element of a WSDL document, or underneath an
     * &lt;element&gt; or &lt;attribute&gt; declaration.
     *
     * @param javaType the Java Class we're writing out schema for
     * @param types the Java2WSDL Types object which holds the context
     *              for the WSDL being generated.
     * @return a type element containing a schema simpleType/complexType
     * @see org.apache.axis.wsdl.fromJava.Types
     */
    public Element writeSchema(Class javaType, Types types) throws Exception {

        // ComplexType representation of bean class
        Element complexType = types.createElement("complexType");

        // See if there is a super class, stop if we hit a stop class
        Element e = null;
        Class superClass = javaType.getSuperclass();
        BeanPropertyDescriptor[] superPd = null;
        List stopClasses = types.getStopClasses();
        if (superClass != null &&
                superClass != java.lang.Object.class &&
                superClass != java.lang.Exception.class &&
                superClass != java.lang.Throwable.class &&
                superClass != java.rmi.RemoteException.class &&
                superClass != org.apache.axis.AxisFault.class &&
                (stopClasses == null ||
                !(stopClasses.contains(superClass.getName()))) ) {
            // Write out the super class
            String base = types.writeType(superClass);
            Element complexContent = types.createElement("complexContent");
            complexType.appendChild(complexContent);
            Element extension = types.createElement("extension");
            complexContent.appendChild(extension);
            extension.setAttribute("base", base);
            e = extension;
            // Get the property descriptors for the super class
            TypeDesc superTypeDesc = TypeDesc.getTypeDescForClass(superClass);
            if (superTypeDesc != null) {
                superPd = superTypeDesc.getPropertyDescriptors();
            } else {
                superPd = MetaBeanUtils.getPd(superClass, null);
            }
        } else {
            e = complexType;
        }

        // Add fields under sequence element.
        // Note: In most situations it would be okay
        // to put the fields under an all element.
        // However it is illegal schema to put an
        // element with minOccurs=0 or maxOccurs>1 underneath
        // an all element.  This is the reason why a sequence
        // element is used.
        Element all = types.createElement("sequence");
        e.appendChild(all);

        if (Modifier.isAbstract(javaType.getModifiers())) {
            complexType.setAttribute("abstract", "true");
        }

        // Serialize each property
        for (int i=0; i<propertyDescriptor.length; i++) {
            String propName = propertyDescriptor[i].getName();

            // Don't serializer properties named class
            boolean writeProperty = true;
            if (propName.equals("class")) {
                writeProperty = false;
            }

            // Don't serialize the property if it is present
            // in the super class property list
            if (superPd != null && writeProperty) {
                for (int j=0; j<superPd.length && writeProperty; j++) {
                    if (propName.equals(superPd[j].getName())) {
                        writeProperty = false;
                    }
                }
            }
            if (!writeProperty) {
                continue;
            }

            Class fieldType = propertyDescriptor[i].getType();

            // If we have type metadata, check to see what we're doing
            // with this field.  If it's an attribute, skip it.  If it's
            // an element, use whatever qname is in there.  If we can't
            // find any of this info, use the default.

            if (typeDesc != null) {
                FieldDesc field = typeDesc.getFieldByName(propName);

                if (field != null) {
                    QName qname = field.getXmlName();
                    QName fieldXmlType = field.getXmlType();
                    boolean isAnonymous = fieldXmlType.getLocalPart().startsWith(">");

                    if (qname != null) {
                        // FIXME!
                        // Check to see if this is in the right namespace -
                        // if it's not, we need to use an <element ref="">
                        // to represent it!!!

                        // Use the default...
                        propName = qname.getLocalPart();
                    }
                    if (!field.isElement()) {
                        writeAttribute(types,
                                       propName,
                                       fieldType,
                                       field.getXmlType(),
                                       complexType);
                    } else {
                        writeField(types,
                                   propName,
                                   fieldType,
                                   propertyDescriptor[i].isIndexed(),
                                   field.isMinOccursZero(),
                                   all, isAnonymous);
                    }
                } else {
                    writeField(types,
                               propName,
                               fieldType,
                               propertyDescriptor[i].isIndexed(), false, all, false);
                }
            } else {
                writeField(types,
                           propName,
                           fieldType,
                           propertyDescriptor[i].isIndexed(), false, all, false);
            }
        }

        // done
        return complexType;
    }
}
