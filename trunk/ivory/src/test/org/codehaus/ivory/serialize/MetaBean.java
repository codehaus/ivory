package org.codehaus.ivory.serialize;

/**
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since May 22, 2003
 */
public class MetaBean
{
    private String hello;
    private String world;
    
    public MetaBean()
    {
    }
    
    /**
     * @return String
     */
    public String getHello()
    {
        return hello;
    }

    /**
     * @axis.hidemethod
     * @return String
     */
    public String getWorld()
    {
        return world;
    }

    /**
     * Sets the hello.
     * @param hello The hello to set
     */
    public void setHello(String hello)
    {
        this.hello = hello;
    }

    /**
     * Sets the world.
     * @param world The world to set
     */
    public void setWorld(String world)
    {
        this.world = world;
    }

}
