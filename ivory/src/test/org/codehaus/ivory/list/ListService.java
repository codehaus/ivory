package org.codehaus.ivory.list;

import java.util.List;

import org.codehaus.ivory.attributes.NonWebMethod;
import org.codehaus.ivory.attributes.ParameterType;

/**
 * Service to test the ability to return the java.util.List class correctly.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 4, 2003
 */
public interface ListService
{
	final public static String ROLE = ListService.class.getName();
	
	/**
     * @@.return ParameterType(String[].class)
     */
	public List getDevelopers();

	/**
	 * @@NonWebMethod()
	 */
	public void secretMethod();	
}
