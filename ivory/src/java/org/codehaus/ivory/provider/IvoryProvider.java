package org.codehaus.ivory.provider;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.codehaus.ivory.IvoryServiceDesc;

/**
 * A provider which intializes with an IvoryServiceDesc instead of a
 * regular ServiceDesc class so we can access the Ivory metadata.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 5, 2003
 */
public class IvoryProvider
    extends RPCProvider
{        
    public void initServiceDesc(SOAPService service, MessageContext msgContext)
            throws AxisFault
    {
        IvoryServiceDesc serviceDescription = new IvoryServiceDesc();
        service.setServiceDescription( serviceDescription );
        
        // Initialize the service description by introspection
        super.initServiceDesc( service, msgContext );
        
        // After axis does it's thing, load our own metadata.    
        serviceDescription.loadMetaData();
    }
}
