package org.codehaus.ivory;

/**
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 11, 2003
 */
public interface SparePartInfo
{
    public static final String ROLE = SparePartInfo.class.getName();
    
    String getPartInfo(String PartSKU)
        throws Exception;
    
    SparePart getSparePart();
}