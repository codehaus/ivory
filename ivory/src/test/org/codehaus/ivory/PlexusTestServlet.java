package org.codehaus.ivory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;

/**
 * PlexusTestServlet
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public class PlexusTestServlet
	extends HttpServlet
{

    public static PlexusContainer Plexus;
    
	public void init() throws ServletException
	{
		super.init();

        getServletContext().setAttribute(PlexusConstants.PLEXUS_KEY, Plexus);
	}
}
