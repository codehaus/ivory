package org.codehaus.ivory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.commons.attributes.Attribute;
import org.apache.commons.attributes.Attributes;

/**
 * Adds meta-data capabilities to Axis's ServiceDesc class.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 5, 2003
 */
public class IvoryServiceDesc
    extends ServiceDesc
{

    protected static final String AXIS_HIDE_METHOD = "axis.hidemethod";
    protected static final String AXIS_SERIALIZE = "axis.serialize.";
    protected static final String OUT_SERIALIZE = "axis.serialize.out";

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
        
        return Attributes.hasAttribute( method, AXIS_HIDE_METHOD );
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
        if ( name == null )
        {
            name = "out";
        }
        
        if ( Attributes.hasAttribute( method, AXIS_SERIALIZE + name ) )
        {
            Attribute outAttribute = 
                Attributes.getAttribute( method, AXIS_SERIALIZE + name );
            
            String clazz = outAttribute.getValue();

            log.debug( "Changing parameter type to " + clazz );
            
            try
            {
                parameter.setJavaType( Class.forName( clazz ) );
            }
            catch ( ClassNotFoundException e )
            {
                log.debug( "Could not find class " + clazz + "." +
                           " Method will not be exposed." );
            }
        }
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
