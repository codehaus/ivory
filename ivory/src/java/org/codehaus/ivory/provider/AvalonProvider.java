/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.codehaus.ivory.provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.rpc.server.ServiceLifecycle;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;

/**
 * Provider class which allows you to specify an Avalon <b>ROLE</b> for
 * servicing Axis SOAP requests.
 *
 * <p>
 *  The specified <b>ROLE</b> corresponds to a particular implementation
 *  which is retrieved by a given Avalon <code>ComponentManager</code>.
 *  For more information about Avalon, see the Avalon.
 *  <a href="http://jakarta.apache.org/avalon">website</a>.
 * </p>
 *
 * <p>
 *  To use this class, you need to add your Avalon <code>ComponentManager</code>
 *  instance to the <code>MessageContext</code> that is Axis uses to process
 *  messages with.
 * </p>
 *
 * <p>
 *  To do this you could for example subclass the AxisServlet and override the
 *  <code>createMessageContext()</code> method adding the ComponentManager, eg:
 *
 *  <pre>
 *   protected MessageContext createMessageContext(...)
 *   {
 *      MessageContext context = super.createMessageContext();
 *      context.setProperty(AvalonProvider.COMPONENT_MANAGER, m_manager);
 *      return context;
 *   }
 *  </pre>
 *
 *  and appropriately add the AvalonProvider to the list of handlers in your
 *  server-config.wsdd (suggestions on how to improve this are welcomed)
 * </p>
 *
 * <p>
 *  This provider will use that <code>ComponentManager</code> reference to
 *  retrieve objects.
 * </p>
 *
 * <p>
 *  In your deployment descriptor use the following syntax:
 *
 * <pre>
 *  &lt;service name="myservice" provider="java:Avalon"&gt;
 *    &lt;parameter name="role" value="my.avalon.role.name"/&gt;
 *    &lt;parameter name="className" value="my.avalon.roles.interface.name"/&gt;
 *    &lt;parameter name="allowedMethods" value="allowed.methods"/&gt;
 *  &lt;/service&gt;
 * </pre>
 *
 * </p>
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @revision CVS $Id: AvalonProvider.java,v 1.2 2004-08-02 14:14:08 dandiep Exp $
 */
public class AvalonProvider
    extends RPCProvider
{
    /**
     * Constant used to retrieve the ServiceManager reference
     * from the MessageContext object.
     */
    public static final String SERVICE_MANAGER = "service-manager";

    /**
     * Constant which represents the name of the ROLE this
     * provider should <i>lookup</i> to service a request with. This is
     * specified in the &lt;parameter name="" value=""/&gt; part of the
     * deployment xml.
     */
    public static final String ROLE = "role";

    /**
     * Returns the service object.
     * 
     * @param msgContext the message context
     * @param role the Avalon ROLE to lookup to find the service object implementation
     * @return an object that implements the service
     * @exception Exception if an error occurs
     */
    protected Object makeNewServiceObject(
        MessageContext msgContext,
        String role)
        throws Exception
    {
        ServiceManager manager =
            (ServiceManager) msgContext
                .getAxisEngine()
                .getApplicationSession()
                .get( SERVICE_MANAGER );

        if (manager == null)
            throw new AxisFault("Could not access Avalon ServiceManager");

        return decorate(manager.lookup(role), manager);
    }

    /**
     * Helper method for decorating a <code>Component</code> with a Handler
     * proxy (see below).
     *
     * @param object a <code>Component</code> instance
     * @param manager a <code>ComponentManager</code> instance
     * @return the <code>Proxy</code> wrapped <code>Component</code> instance
     * @exception Exception if an error occurs
     */
    private Object decorate(final Object object, final ServiceManager manager)
        throws Exception
    {
        // obtain a list of all interfaces this object implements
        Class[] interfaces = object.getClass().getInterfaces();

        // add ServiceLifecycle to it
        Class[] adjusted = new Class[interfaces.length + 1];
        System.arraycopy(interfaces, 0, adjusted, 0, interfaces.length);
        adjusted[interfaces.length] = ServiceLifecycle.class;

        // create a proxy implementing those interfaces
        Object proxy =
            Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                adjusted,
                new Handler(object, manager));

        // return the proxy
        return proxy;
    }

    /**
     * Get the service class description
     * 
     * @param role the Avalon ROLE name
     * @param service a <code>SOAPService</code> instance
     * @param msgContext the message context
     * @return service class description
     * @exception AxisFault if an error occurs
     */
    protected Class getServiceClass(
        String role,
        SOAPService service,
        MessageContext msgContext)
        throws AxisFault
    {
        // Assuming ExcaliburComponentManager semantics the ROLE name is
        // actually the class name, potentially with a variant following
        // the class name with a '/' separator

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String className = role;
        
        int i;
        
        if ((i = role.indexOf('/')) != -1)
        {
            className = role.substring(0, i);
        }
        
        try
        {
            return cl.loadClass( className );
        }
        catch (ClassNotFoundException e)
        {
            throw new AxisFault( "Couldn't find class " + className, e );
        }
    }

    /**
     * <code>InvocationHandler</code> class for managing Avalon
     * <code>Components</code>.
     *
     * <p>
     *  Components retrieved from an Avalon ComponentManager must be
     *  returned to the manager when they are no longer required.
     * </p>
     *
     * <p>
     *  The returning of Components to their ComponentManager is handled
     *  by a Proxy class which uses the following InvocationHandler.
     * </p>
     *
     * <p>
     *  Each Component returned by this Provider is wrapped inside a 
     *  Proxy class which implements all of the Component's interfaces
     *  including javax.xml.rpc.server.ServiceLifecycle.
     * </p>
     *
     * <p>
     *  When Axis is finished with the object returned by this provider,
     *  it invokes ServiceLifecycle.destroy(). This is intercepted by the
     *  InvocationHandler and the Component is returned at this time back
     *  to the ComponentManager it was retrieved from.
     * </p>
     *
     * <p>
     *  <b>Note</b>, when Axis invokes ServiceLifecycle.destroy() is dependant
     *  on the scope of the service (ie. Request, Session & Application).
     * </p>
     */
    final class Handler implements InvocationHandler
    {
        // Constants describing the ServiceLifecycle.destroy method
        private final String SL_DESTROY = "destroy";
        private final Class SL_CLASS = ServiceLifecycle.class;

        // Component & ServiceManager references
        private final Object m_object;
        private final ServiceManager m_manager;

        /**
         * Simple constructor, sets all internal references
         *
         * @param object a <code>Component</code> instance
         * @param manager a <code>ComponentManager</code> instance
         * @param log a <code>Logger</code> instance
         */
        public Handler(final Object object, final ServiceManager manager)
        {
            m_object = object;
            m_manager = manager;
        }

        /**
         * <code>invoke</code> method, handles all method invocations for this
         * particular proxy.
         *
         * <p>
         *  Usually the invocation is passed through to the
         *  actual component the proxy wraps, unless the method belongs to
         *  the <code>ServiceLifecycle</code> interface where it is handled
         *  locally.
         * </p>
         *
         * @param proxy the <code>Proxy</code> instance the method was invoked on
         * @param method the invoked method <code>Method</code> object
         * @param args an <code>Object[]</code> array of arguments
         * @return an <code>Object</code> value or null if none
         * @exception Throwable if an error occurs
         */
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            try
            {
                // if ServiceLifecycle.destroy() called, return to CM
                if (method.getDeclaringClass().equals(SL_CLASS))
                {
                    if (method.getName().equals(SL_DESTROY))
                    {
                        m_manager.release(m_object);
                    }

                    return null;
                }
                else // otherwise pass call to the real object
                {
                    return method.invoke(m_object, args);
                }
            }
            catch ( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
