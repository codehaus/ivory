package org.codehaus.ivory;

import org.apache.axis.AxisFault;
import org.apache.axis.server.AxisServer;

/**
 * A service exposing Axis.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 8, 2003
 */
public interface AxisService
{
    static final String ROLE = AxisService.class.getName();
    
    /** Key to retrieve the ServiceManager from the MessageContext */
    static final String SERVICE_MANAGER_KEY = "ivory.service-manager";
    
    /**
     * Get the AxisServer.
     * 
     * @return AxisServer
     */
    AxisServer getAxisServer();
    
    /**
     * Exposes a class as a SOAP service.  All methods are available to be
     * executed.
     * 
     * @param classService
     */
	public void exposeClass( String serviceName, String className )
        throws AxisFault, ClassNotFoundException;
	
    /**
     * Exposes a class as a SOAP service.  Only the methods specified are
     * exposed.  If methodNames is null, then all methods are exposed.
     * 
     * @param methods
     * @param classService
     */
	public void exposeClass( String serviceName,
	                         String[] methodNames, 
	                         String className )
        throws AxisFault, ClassNotFoundException;
    
    /**
     * Exposes an Avalon component as a SOAP service.  All methods are available
     * to be executed.
     * 
     * @param classService
     */
    public void exposeService( String serviceName, String role )
        throws AxisFault, ClassNotFoundException;
    
     /**
      * Exposes an Avalon component as a SOAP service.  Only the methods 
      * specified are exposed.  If methodNames is null, then all methods are 
      * exposed.
      * 
      * @param methods
      * @param classService
      */
    public void exposeService( String serviceName,
                               String[] methodNames, 
                               String role )
        throws AxisFault, ClassNotFoundException;
}
