package org.codehaus.ivory;

/**
 * This class represents a SparePart and is used to make sure that we
 * can send complex objects over the wire.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 12, 2003
 */
public class SparePart
{
    private String sku;

    public SparePart()
    {
    }
    
    public SparePart( String sku )
    {
        this.sku = sku;
    }
    
    public String getSKU()
    {
        return sku;
    }
}
