package org.codehaus.ivory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		res.getOutputStream().println("hello.");
	}
    
    public static PlexusContainer Plexus;
    
	public void init() throws ServletException
	{
		super.init();

        getServletContext().setAttribute(PlexusConstants.PLEXUS_KEY, Plexus);
	}
}
