package org.codehaus.ivory.list;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 4, 2003
 */
public class DefaultListService
	implements ListService
{

    /**
     * @see org.codehaus.ivory.axis.list.ListService#getDevelopers()
     * @@ParameterType("[java.lang.String;")
     */
    public List getDevelopers()
    {
        List list = new ArrayList();
        list.add( "Dan Diephouse" );
        list.add( "Jason van Zyl" );
        
        return list;
    }

    /**
     * @see org.codehaus.ivory.axis.list.ListService#secretMethod()
     */
    public void secretMethod()
    {
    }
}
