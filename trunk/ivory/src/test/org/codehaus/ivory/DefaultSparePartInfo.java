package org.codehaus.ivory;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 11, 2003
 */
public class DefaultSparePartInfo
    implements SparePartInfo
{
    /** Creates new SparePartPriceService */
    public DefaultSparePartInfo()
    {
    }

    public String getPartInfo(String PartSKU) throws Exception
    {
        return PartSKU + " - Part Info";
    }

    /**
     * @see org.codehaus.ivory.axis.SparePartInfo#getSparePart()
     */
    public SparePart getSparePart()
    {
        return new SparePart( "part123" );
    }

}
