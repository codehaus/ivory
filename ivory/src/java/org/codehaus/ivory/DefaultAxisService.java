package org.codehaus.ivory;

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.axis.AxisFault;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.encoding.TypeMappingImpl;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.enum.Scope;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.server.AxisServer;
import org.apache.axis.wsdl.fromJava.Namespaces;
import org.codehaus.ivory.provider.AvalonProvider;
import org.codehaus.ivory.provider.IvoryAvalonProvider;
import org.codehaus.ivory.provider.IvoryProvider;
import org.codehaus.ivory.provider.WSDDJavaAvalonProvider;

/**
 * The default AxisService implementation.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Mar 9, 2003
 */
public class DefaultAxisService 
    extends AbstractLogEnabled
    implements AxisService, Startable, Configurable, Initializable, Serviceable
{

    public static final QName QNAME_AVALONRPC_PROVIDER =
        new QName(WSDDConstants.URI_WSDD_JAVA, "Avalon");
    
    private ServiceManager manager;

    protected static final String SERVER_CONFIG_KEY = "server-config";
    
    protected static final String DEFAULT_SERVER_CONFIG = 
    	"/org/codehaus/ivory/server-config.wsdd";
    
    private SimpleProvider provider;
    
    private AxisServer axisServer;
    
    private String serverConfig;
    
    private Configuration services;

    /**
     * @param configuration
     * @throws ConfigurationException
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure( Configuration configuration )
        throws ConfigurationException
    {
        serverConfig = configuration.getAttribute( SERVER_CONFIG_KEY, "" );
        
        services = configuration.getChild( "services" );
    }
    
	/**
	 * @throws Exception
	 * @see org.apache.avalon.framework.activity.Initializable#initialize()
	 */
	public void initialize() throws Exception
	{
		initializeAxisServer();
        initializeWSDDProviders();
		   
		// Initialize the services in the configuration.
		initializeServices( services );
        
        // This is definitely not what setOption was meant for...
        getAxisServer().setOption(SERVICE_MANAGER_KEY, manager);
	}
    
    /**
     * Register custom providers with Axis.
     */
    private void initializeWSDDProviders()
    {
        /* This could be registered other ways - like through the jar's
         * META-INF, but lets take the easy and straightforward way out.
         */
        WSDDProvider.registerProvider(
            QNAME_AVALONRPC_PROVIDER,
            new WSDDJavaAvalonProvider());
    }

    /**
     * @param services
     */
    protected void initializeServices(Configuration services)
        throws Exception
    {
        initializeClassServices( services.getChildren( "classService" ) );
        
        initializeAvalonServices( services.getChildren( "avalonService" ) );  
    }

    /**
     * @param configurations
     */
    protected void initializeClassServices( Configuration[] services )
        throws Exception
    {
        for ( int i = 0; i < services.length; i++ )
        {
            String name = services[i].getAttribute( "name" );
            String role = services[i].getAttribute( "class" );
            exposeClass( name, role );
        }   
    }

    /**
     * @param configurations
     */
    protected void initializeAvalonServices(Configuration[] services )
        throws Exception
    {
        for ( int i = 0; i < services.length; i++ )
        {
            String name = services[i].getAttribute( "name" );
            String className = services[i].getAttribute( "role" );
            exposeService( name, className );
        }
    }

	/**
	 * Initializes the AxisServer.
	 * @throws Exception
	 */
    public void initializeAxisServer() throws Exception
    {
		/* Technically, we are supposed to use Axis's EngineConfigurationFactory
		 * but it is a big PITA and seems uneccessary.  This can be changed
		 * in the future if more flexible mechanisms of loading the
		 * configuration are needed.
		 */
		FileProvider fileProvider = null;

    	if ( serverConfig.equals("") )
    	{
    		getLogger().debug( "Using default server-config.wsdd." );
    		
    		InputStream is = this.getClass().getResourceAsStream( DEFAULT_SERVER_CONFIG );
			
			if ( is == null )
				throw new RuntimeException( "Configuration is null!" );

    		fileProvider = new FileProvider( DEFAULT_SERVER_CONFIG );
    		fileProvider.setInputStream( is );
    	}
    	else
    	{
			getLogger().debug( "Using server-config " + serverConfig + "." );
    		
    		fileProvider = new FileProvider( serverConfig );
    	}

        /* Wrap the FileProvider with a SimpleProvider.  This needs to be done
         * because the only way to expose services with the FileProvider is to
         * use WSDD deployment descriptors.  SimpleProvider allows us to deploy
         * services much easier.
         */
        provider = new SimpleProvider( fileProvider );

    	// Create the AxisServer from the configuraiton.
        axisServer = new AxisServer( provider );
        axisServer.getApplicationSession().set( AvalonProvider.SERVICE_MANAGER,
                                                manager );
    }
   
    /**
     * @throws Exception
     * @see org.apache.avalon.framework.activity.Startable#start()
     */
    public void start() throws Exception
    {
    	getLogger().debug( "Starting " + DefaultAxisService.ROLE );
    	
    	// This doesn't need to be done, since the AxisServier constructor
    	// calls init(), which is what start() does.
        // axisServer.start();
    }
    
    /**
     * @throws Exception
     * @see org.apache.avalon.framework.activity.Startable#stop()
     */
    public void stop() throws Exception
    {
		getLogger().debug( "Stopping " + DefaultAxisService.ROLE );
    	
    	axisServer.stop();        
    }
    
    /**
     * @return AxisServer
     * @see org.codehaus.ivory.axis.AxisService#getAxisServer()
     */
    public AxisServer getAxisServer()
    {
        return axisServer;
    }
    
    /**
     * @see org.codehaus.ivory.axis.AxisService#exposeClass(java.lang.String, java.lang.Class)
     */
    public void exposeClass( String serviceName, String classService )
        throws AxisFault, ClassNotFoundException
    {
    	exposeClass( serviceName, null, classService );
    }
    
    /**
     * @see org.codehaus.ivory.axis.AxisService#exposeClass(java.lang.String, java.lang.String[], java.lang.Class)
     */
    public void exposeClass( String serviceName,
    					     String[] methodNames, 
                             String className )
        throws AxisFault, ClassNotFoundException
    {
    	SOAPService service = new SOAPService( new IvoryProvider() );
        
        initializeService( service, serviceName,
                           methodNames, className );
            
        getLogger().debug( "Exposed class " + className + 
                           " as " + serviceName + "." );
    }

    /**
     * @see org.codehaus.ivory.axis.AxisService#exposeService(java.lang.String, java.lang.String)
     */
    public void exposeService(String serviceName, String role)
        throws AxisFault, ClassNotFoundException
    {
        exposeService( serviceName, null, role ); 
    }

    /**
     * @see org.codehaus.ivory.axis.AxisService#exposeService(java.lang.String, java.lang.String[], java.lang.String)
     */
    public void exposeService(String serviceName, 
                              String[] methodNames, 
                              String role)
        throws AxisFault, ClassNotFoundException
    {
        SOAPService service = new SOAPService( new IvoryAvalonProvider() );
        
        initializeService( service, serviceName, methodNames, role );
            
        getLogger().debug( "Exposed service " + role + 
                           " as " + serviceName + "." );
    }

    /**
     * Initializes the SOAPService with the appropriate information.
     */
    protected void initializeService( SOAPService service,
                                      String serviceName, 
                                      String[] methodNames, 
                                      String className )
        throws AxisFault, ClassNotFoundException
    {
        service.setEngine( getAxisServer() );
        
        // The namespace of the service.
        String namespace =  Namespaces.makeNamespace( className );
        
        /* Now we set up the various options for the SOAPService. We set:
         * 
         * RPCProvider.OPTION_WSDL_SERVICEPORT
         * In essense, this is our service name
         * 
         * RPCProvider.OPTION_CLASSNAME
         * This tells the provider (whether it be an AvalonProvider or just
         * JavaProvider) what class to load via "makeNewServiceObject".
         * 
         * RPCProvider.OPTION_SCOPE
         * How long the object loaded via "makeNewServiceObject" will persist -
         * either request, session, or application.  We use the default for now.
         * 
         * RPCProvider.OPTION_WSDL_TARGETNAMESPACE
         * A namespace created from the package name of the service.
         * 
         * RPCProvider.OPTION_ALLOWEDMETHODS
         * What methods the service can execute on our class.
         * 
         * We don't set:
         * RPCProvider.OPTION_WSDL_PORTTYPE
         * RPCProvider.OPTION_WSDL_SERVICEELEMENT
         */
        service.setOption( RPCProvider.OPTION_WSDL_SERVICEPORT, serviceName );
        service.setOption( RPCProvider.OPTION_CLASSNAME, className );
        service.setOption( RPCProvider.OPTION_SCOPE, Scope.DEFAULT.getName());
        service.setOption( RPCProvider.OPTION_WSDL_TARGETNAMESPACE, 
                           namespace  );
                    
        // Set the allowed methods, allow all if there are none specified.
        if ( methodNames == null)
        {
            service.setOption( RPCProvider.OPTION_ALLOWEDMETHODS, "*" );
        }
        else
        {
            service.setOption( RPCProvider.OPTION_ALLOWEDMETHODS, methodNames ); 
        }
                
        /* Create a service description.  This tells Axis that this
         * service exists and also what it can execute on this service.  It is
         * created with all the options we set above.
         */
        ServiceDesc sd = service.getInitializedServiceDesc(null);
		
        // Tell Axis to try and be intelligent about serialization.
        TypeMappingRegistry registry = service.getTypeMappingRegistry();        
        
        TypeMappingImpl tm = (TypeMappingImpl) registry.getDefaultTypeMapping();
        tm.setDoAutoTypes( true );
        
        // Tell the axis configuration about our new service.
        provider.deployService( serviceName, service );
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException
    {
        this.manager = manager;
    }
    
    public ServiceManager getServiceManager()
    {
        return manager;
    }
}
