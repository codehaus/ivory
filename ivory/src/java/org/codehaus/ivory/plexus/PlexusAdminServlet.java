package org.codehaus.ivory.plexus;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AdminServlet;
import org.codehaus.ivory.AxisService;
import org.codehaus.ivory.DefaultAxisService;

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
        return DefaultAxisService.getServiceManager();
    }
    
    public void destroy()
    {
    	super.destroy();
    	
        if ( axisService != null )
        	manager.release( axisService );
    }
    
    protected String getOption(ServletContext context,
			String param,
			String dephault)
	{
		String value = AxisProperties.getProperty(param);

		if (value == null) value = getInitParameter(param);

		if (value == null) value = context.getInitParameter(param);
		try
		{
			AxisServer engine = getEngine();
			if (value == null && engine != null) value = (String) engine
					.getOption(param);
		}
		catch (AxisFault axisFault)
		{
		}

		return (value != null) ? value : dephault;
	}
}
