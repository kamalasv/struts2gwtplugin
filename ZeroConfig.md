# Introduction #

One of the most exciting features of Struts 2 is the ability to use the framework with no configuration. All you have to do is add the servlet to your web.xml file and go. Unfortunately due to bug [WW-2016](http://issues.apache.org/struts/browse/WW-2016) the interceptor used by this plug-in cannot be referenced in the zero configuration setup. To work around this you can get very close to zero configuration using wildcard values in your action mappings.

This page assumes the reader is already with Struts' zero configuration setup, if you are not please refer to [here](http://struts.apache.org/2.x/docs/zero-configuration.html). This page also assumes you are familiar with Struts' wildcard mapping functionality; if you need more information on wildcard usage see [here](http://struts.apache.org/2.x/docs/wildcard-mappings.html).


# Using Wildcard Values with Struts2GWTPlugin #

Normally in a zero configuration setup there would be no changes to your `struts.xml` file. Unfortunately (as of Struts 2.08) the zero configuration setup defaults to the use of the default interceptor stack. This stack by passes the GWT interceptor used by this plug-in, so  the actions GWT calls return the standard Struts response instead of the RPC object your application expects. To work around this define a default action in your `struts.xml`:

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE struts PUBLIC
    "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
    "http://struts.apache.org/dtds/struts-2.0.dtd">

<struts>

  <constant name="struts.enable.DynamicMethodInvocation" value="false" />
  <constant name="struts.devMode" value="true" />

   <package name="mypack" extends="gwt-default">

     <action name="*" class="{1}">
        <interceptor-ref name="gwt"/>
     </action>

  </package>

</struts>
```

This maps all actions to the Struts2GWTPlugin, this can be overridden by adding specific mapping for actions that require different interceptors or results. The drawback of this approach is that in your call to the action you must specify the full package name like this:

```
    ServiceDefTarget endpoint = (ServiceDefTarget) service;
    endpoint.setServiceEntryPoint("com.googlecode.server.actions.DoMyAction.action");
```

**this will NOT work:**

```
    ServiceDefTarget endpoint = (ServiceDefTarget) service;
    endpoint.setServiceEntryPoint("DoMyAction.action");
```

Optionally, if you know all of your actions reside in a specific package then you can specify your mapping like this:

```
    ...

     <action name="*" class="com.googlecode.server.actions.{1}">
        <interceptor-ref name="gwt"/>
     </action>

    ...
```

Using the definition above and based on the previous example, this **will** work:

```
    ServiceDefTarget endpoint = (ServiceDefTarget) service;
    endpoint.setServiceEntryPoint("DoMyAction.action");
```

Using wildcard values gets you very close to zero configuration and provides the ability to override the default behavior.


