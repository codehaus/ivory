package org.codehaus.ivory;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.XMLType;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * A client for the SparePartInfo service.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 11, 2003
 */
public class SparePartInfoClient
{
    public String getPartInfo(String PartSKU) throws Exception
    {
        // EndPoint URL for the SparePartInfo  Web Service
        String endpointURL = "http://localhost/services/SparePartInfo";
        
        // Method Name to invoke for the SparePartInfo Web Service
        String methodName  = "getPartInfo";
        
        // Create the Service call
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(new java.net.URL(endpointURL));
        call.setOperationName(new QName("SparePartInfo",methodName));
        call.addParameter( "sku",
                           XMLType.XSD_STRING,
                           ParameterMode.IN );
                           
        call.setReturnType( XMLType.XSD_FLOAT );
      
        //Setup the Parameters i.e. the Part SKU to be passed as input parameter to th
        //SparePartInfo Web Service
        Object[] params = new Object[] { PartSKU };
      
        //Invoke the SparePartInfo Web Service
        return (String) call.invoke(params);
    }
}
