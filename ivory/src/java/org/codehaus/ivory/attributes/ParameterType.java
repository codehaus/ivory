package org.codehaus.ivory.attributes;

/**
 * An Attribute which specifies the returned parameter type for SOAP
 * serialization.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Sep 23, 2003
 */
public class ParameterType
{
    private Class clazz;
    
    public ParameterType( Class clazz )
    {
        this.clazz = clazz;
    }
    
    public Class getParameterType()
    {
        return clazz;
    }
}
