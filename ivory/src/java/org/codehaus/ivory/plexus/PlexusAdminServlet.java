package org.codehaus.ivory.plexus;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.axis.AxisFault;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AdminServlet;
import org.apache.plexus.servlet.PlexusServlet;
import org.codehaus.ivory.AxisService;

/**
 * An implementation of the Axis AdminServlet which retrieves the AxisEngine
 * from the ServiceManager.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 8, 2003
 */
public class PlexusAdminServlet 
    extends AdminServlet
{
	ServiceManager manager;
	
	AxisService axisService;
	
    public PlexusAdminServlet()
    {
    }
    
    /**
     * Provide the AxisEngine to the base servlet class.
     * 
     * @return AxisServer
     * @see org.apache.axis.transport.http.AxisServletBase#getEngine()
     */
    public AxisServer getEngine() throws AxisFault
    {
        manager = getServiceManager();
        
        try
        {
            axisService = ( AxisService ) manager.lookup( AxisService.ROLE );
        }
        catch (ServiceException e)
        {
            throw new AxisFault( "Could not find the AxisService.", e );
        }
        
        return axisService.getAxisServer();
    }
    
    /**
     * Retrieve the ServiceBroker from the ServletContext.  This presupposes
     * that the installation is using Plexus.
     * 
     * @return ServiceBroker
     */
    public ServiceManager getServiceManager()
    {
        return (ServiceManager) getServletContext().getAttribute( 
            PlexusServlet.SERVICE_MANAGER_KEY );
    }
    
    public void destroy()
    {
    	super.destroy();
    	
        if ( axisService != null )
        	manager.release( axisService );
    }
}
