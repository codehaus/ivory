package org.codehaus.ivory.list;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.ivory.attributes.NonWebMethod;
import org.codehaus.ivory.attributes.ParameterType;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 4, 2003
 */
public class DefaultListService
	implements ListService
{
	private NonWebMethod nwm;
    private ParameterType pt;
    
    public List getDevelopers()
    {
        List list = new ArrayList();
        list.add( "Dan Diephouse" );
        list.add( "Jason van Zyl" );
        
        return list;
    }

    public void secretMethod()
    {
    }
}
