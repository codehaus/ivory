package org.codehaus.ivory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.commons.attributes.Attributes;
import org.codehaus.ivory.attributes.NonWebMethod;
import org.codehaus.ivory.attributes.ParameterType;

/**
 * Adds meta-data capabilities to Axis's ServiceDesc class.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 5, 2003
 */
public class IvoryServiceDesc
    extends JavaServiceDesc
{
    private boolean loadingServiceDesc = false;
    
    /**
     * Load the meta data for the class being exposed as a service and take
     * apropriate action.
     */
    public void loadMetaData()
    {      
        for ( Iterator itr = getOperations().iterator(); itr.hasNext(); )
        {
            OperationDesc operation = ( OperationDesc ) itr.next();
            
            customizeOperation( operation );
            
            if ( !isValidOperation( operation ) ||
                 hideOperation( operation ) )
            {
                itr.remove();
            }
        }
    }

    /**
     * Whether or an Operation on the services should not be exposed.
     * 
     * @param operation
     * @return
     */
    protected boolean hideOperation(OperationDesc operation)
    {
        Method method = operation.getMethod();
        
        return Attributes.hasAttribute( method, NonWebMethod.class );
    }

    protected void customizeOperation( OperationDesc operation )
    {
        for( Iterator itr = operation.getParameters().iterator(); itr.hasNext(); )
        {
            ParameterDesc parameter = (ParameterDesc) itr.next();
            
            customizeParameter( operation, parameter ); 
        }
        
        customizeParameter( operation, operation.getReturnParamDesc() );
    }

    protected void customizeParameter( OperationDesc operation, 
                                     ParameterDesc parameter )
    {
        Method method = operation.getMethod();
        
        String name = parameter.getName();
        
        if ( name == null &&
             Attributes.hasReturnAttributeType( method, ParameterType.class ) )
        {
            Collection allAttrs = Attributes.getReturnAttributes(method);
            
            for ( Iterator itr = allAttrs.iterator(); itr.hasNext(); )
            {
                Object attr = itr.next();
                if ( attr instanceof ParameterType )
                {
                    changeParameterType( parameter, (ParameterType) attr );        
                }
            }
        }
        else if (Attributes.hasReturnAttributeType( method, ParameterType.class ))
        {
            // parameters are named "in0", "in1" and so on.
            // get everything after "in"
            int num = new Integer(name.substring( 2 )).intValue();
            Collection allAttrs = Attributes.getParameterAttributes(method, num);
            
            for ( Iterator itr = allAttrs.iterator(); itr.hasNext(); )
            {
                Object attr = itr.next();
                if ( attr instanceof ParameterType )
                {
                    changeParameterType( parameter, (ParameterType) attr );        
                }
            }
        }
    }

    /**
     * @param parameter
     * @param type
     */
    private void changeParameterType(ParameterDesc parameter, ParameterType type)
    {            
        String clazz = type.getParameterType().getName();

        log.debug( "Changing parameter type to " + clazz );
            
        parameter.setJavaType( type.getParameterType() );
    }

    protected boolean isValidOperation( OperationDesc operation )
    {
        for( Iterator itr = operation.getParameters().iterator(); itr.hasNext(); )
        {
            ParameterDesc parameter = (ParameterDesc) itr.next();
            
            if ( !isValidParameter( parameter ) )
                return false;   
        }
        
        return isValidParameter( operation.getReturnParamDesc() );
    }

    /**
     * Checks whether or not Axis will work with this parameter.  The only
     * criterion is that it not be a List or inherit from the List type.
     * 
     * @param parameter
     * @return
     */
    protected boolean isValidParameter( ParameterDesc parameter )
    {
        if ( parameter.getJavaType().equals( java.util.List.class.getClass() )
             || List.class.isAssignableFrom( parameter.getJavaType() ) )
        {
            return false;
        }
        
        return true;
    }
}
