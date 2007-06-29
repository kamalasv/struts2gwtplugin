/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.googlcode.strut2gwtplugin.interceptor;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * This class is a modified version of GWT's RemoteServiceServlet.java
 */
class GWTServlet extends RemoteServiceServlet {

    /** Context for the servlet */
    private ServletContext servletContext;
    
    /** Action class to invoke */
    private ActionInvocation actionInvocation;

    /**
     * Find the invoked method on either the specified interface or any super.
     */
    private static Method findInterfaceMethod(Class intf, String methodName,
        Class[] paramTypes, boolean includeInherited) {
        try {
            return intf.getDeclaredMethod(methodName, paramTypes);
        } catch(NoSuchMethodException e) {
            if(includeInherited) {
                Class[] superintfs = intf.getInterfaces();
                for(int i = 0; i < superintfs.length; i++) {
                    Method method = findInterfaceMethod(superintfs[i],
                        methodName, paramTypes, true);
                    if(method != null) {
                        return method;
                    }
                }
            }

            return null;
        }
    }

    /**
     * The default constructor.
     */
    public GWTServlet() {
        super();
    }

    /* (non-Javadoc)
     * @see com.google.gwt.user.server.rpc.RemoteServiceServlet#processCall(java.lang.String)
     */
    public String processCall(String payload) throws SerializationException {
        
        // default return value - Should this be something else?
        // no known constants to use in this case
        String result = null; 
        
        // get the RPC Request from the request data
        RPCRequest rpcRequest= RPC.decodeRequest(payload);
        
        // get the parameter types for the method look-up
        Class[] paramTypes = new Class[rpcRequest.getParameters().length];
        for (int i=0; i < paramTypes.length; i++) {
            paramTypes[i] = rpcRequest.getParameters()[i].getClass();
        }
        
        // we need to get the action method from Struts
        Method method = findInterfaceMethod(
                actionInvocation.getAction().getClass(), 
                rpcRequest.getMethod().getName(), 
                paramTypes, true);
        
        // if the method is null, this may be a hack attempt
        // or we have some other big problem
        if (method == null) {
            // present the params
            StringBuffer params = new StringBuffer();
            for (int i=0; i < paramTypes.length; i++) {
                params.append(paramTypes[i]);
                if (i < paramTypes.length-1) {
                    params.append(", ");
                }
            }
            
            // throw a security exception, could be attempted hack
            //
            // XXX - should we package up the failure like we do below? 
            //
            throw new SecurityException(
                    "Failed to locate method "+ rpcRequest.getMethod().getName()
                    + "("+ params +") on interface "
                    + actionInvocation.getAction().getClass().getName()
                    + " requested through interface "
                    + rpcRequest.getClass().getName());
        }
        
        // now make the call
        Object callResult = null;
        try {
            callResult = method.invoke(actionInvocation.getAction(), 
                    rpcRequest.getParameters());
        } catch (IllegalAccessException iie) {
            // This may need to change this to package up up the cause
            // instead of the thrown exception, not sure if the 
            // chaining will translate
            result = RPC.encodeResponseForFailure(method, iie);
        } catch (InvocationTargetException ite) {
            // This may need to change this to package up up the cause
            // instead of the thrown exception, not sure if the 
            // chaining will translate
            result = RPC.encodeResponseForFailure(method, ite);
        }
        
        // if we got a respone, package it up for GWT
        if (callResult != null) {
            result = RPC.encodeResponseForSuccess(method, callResult);
        }
        
        // return our response
        return result;
    }

    /**
     * Returns the servlet's context
     * 
     * @return a <code>ServletContext</code>
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Sets the servlet's context
     * 
     * @param servletContext <code>ServletContext</code> to use
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Returns the action class to invoke
     * 
     * @return an <code>ActionInvocation</code>
     */
    public ActionInvocation getActionInvocation() {
        return actionInvocation;
    }

    /**
     * Sets the action class to call
     * 
     * @param actionInvocation <code>ActionInvocation</code> to use
     */
    public void setActionInvocation(ActionInvocation actionInvocation) {
        this.actionInvocation = actionInvocation;
    }
}
