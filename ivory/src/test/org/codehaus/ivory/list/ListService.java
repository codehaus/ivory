package org.codehaus.ivory.list;

import java.util.List;

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
     * @axis.serialize.out [Ljava.lang.String;
     */
	public List getDevelopers();

	/**
	 * @axis.hidemethod
	 */
	public void secretMethod();	
}
