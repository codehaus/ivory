package org.codehaus.ivory.plexus;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.ConfigurationException;
import org.apache.axis.MessageContext;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.axis.utils.Messages;
import org.codehaus.ivory.AxisService;
import org.codehaus.ivory.DefaultAxisService;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.personality.avalon.AvalonServiceManager;
import org.codehaus.plexus.servlet.PlexusServletUtils;

/**
 * An implementation of the Axis AxisServlet which retrieves the AxisEngine
 * from the ServiceManager.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 8, 2003
 */
public class PlexusAxisServlet 
    extends AxisServlet
{
	ServiceManager manager;
	
	AxisService axisService;
	
	public PlexusAxisServlet()
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

    /**
     * respond to the ?list command.
     * if enableList is set, we list the engine config. If it isnt, then an
     * error is written out
     * @param response
     * @param writer
     * @throws AxisFault
     */
//    protected void processListRequest( HttpServletResponse response, 
//                                       PrintWriter writer )
//        throws AxisFault 
//    {
//        AxisEngine engine = getEngine();
//
//        boolean enableList = true;
//        
//        if (enableList) {
//            if ( engine.getConfig() instanceof WSDDEngineConfiguration )
//            {
//                super.processListRequest( response, writer );
//            }
//            else if ( engine.getConfig() instanceof SimpleProvider )
//            {
//                SimpleProvider config = ( SimpleProvider ) engine.getConfig();
//                
//                Iterator itr;
//                try
//                {
//                    itr = config.getDeployedServices();
//                }
//                catch (ConfigurationException e)
//                {
//                    throw new AxisFault( "Configuration error.", e );
//                }
//                
//                response.setContentType("text/html");
//                writer.println("<h2>Services</h2>");
//                for ( SOAPService service = (SOAPService) itr.next();
//                    itr.hasNext(); )
//                {
//                    writer.println("<p>" +
//                                   service.getName() +
//                                   "</p>");
//                }
//            }
//        } 
//        else 
//        {
//            // list not enable, return error
//            //error code is, what, 401
//            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
//            response.setContentType("text/html");
//            writer.println("<h2>" +
//                           Messages.getMessage("error00") +
//                           "</h2>");
//            writer.println("<p><i>?list</i> " +
//                           Messages.getMessage("disabled00") +
//                           "</p>");
//        }
//    }
    
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
