package org.codehaus.ivory.plexus;

import javax.servlet.ServletContext;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AdminServlet;
import org.codehaus.ivory.AxisService;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

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
	PlexusContainer manager;
	
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
        manager = getPlexusContainer();
                
        try
        {
            axisService = ( AxisService ) manager.lookup( AxisService.ROLE );
        }
        catch (ComponentLookupException e)
        {
            throw new AxisFault( "Could not find the AxisService.", e );
        }
        
        return axisService.getAxisServer();
    }
    
    /**
     * Retrieve the PlexusContainer from the ServletContext.
     * 
     * @return ServiceBroker
     */
    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }
    
    public void destroy()
    {
    	super.destroy();
    	
        if ( axisService != null ) try
		{
			manager.release( axisService );
		}
		catch (Exception e)
		{
			log("Couldn't release AxisService.", e);
		}
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
