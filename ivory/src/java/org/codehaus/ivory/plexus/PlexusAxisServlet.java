package org.codehaus.ivory.plexus;

import javax.servlet.ServletContext;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AxisServlet;
import org.codehaus.ivory.AxisService;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

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
	PlexusContainer manager;
    
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
