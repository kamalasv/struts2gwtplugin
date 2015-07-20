## Install plugin ##
  1. Copy the downloaded jar into the "WEB-INF/lib" folder
  1. Copy gwt-servlet.jar (from GWT) into the "WEB-INF/lib" folder

## Setup Action ##
Start by creating an action that contains the method that is going to be called. This method needs to match the signature of the service interface, but you don't need to implement the interface on the action.  This method will be different from regular Struts 2 action methods in that it returns any object (that GWT can serialize), instead of a result name. If you want to call regular actions from GWT (not using GWT's RPC) go [here](http://cwiki.apache.org/confluence/pages/viewpage.action?pageId=34036)

In this example the action "Hello" will return the string passed to it. Remember, **the returned object is not the regular Struts result**, it is the object that you will receive on the client side. The method can have any name.

Example:
```
public class Hello {
    public String execute(String text) {
        return text;
    }
}
```

## Write the mapping for the action ##
For any action mapping that is going to be called from GWT:
  1. Add the map inside a package that extends "gwt-default"
  1. Add the "gwt" interceptor to the mapping

Example:
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE struts PUBLIC
    "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
    "http://struts.apache.org/dtds/struts-2.0.dtd">

<struts>

  <constant name="struts.enable.DynamicMethodInvocation" value="false" />
  <constant name="struts.devMode" value="true" />

  <package name="example"  extends="gwt-default">
     <action name="Hello" class="example.Hello">
        <interceptor-ref name="gwt"/>
     </action>
  </package>

</struts>
```

## GWT classes ##
Write the required service interface as described [here](http://code.google.com/webtoolkit/documentation/com.google.gwt.doc.DeveloperGuide.RemoteProcedureCalls.MakingACall.html). This is regular GWT stuff.

As an example of the interface:
```
public interface MyService extends RemoteService {
    public String execute(String s);
}

public interface MyServiceAsync {
    public void execute(String s, AsyncCallback callback);
}
```

And to call it:
```
MyServiceAsync service = (MyServiceAsync) GWT.create(MyService.class);
AsyncCallback callback = new AsyncCallback() {
    public void onSuccess(Object result) {
        Window.alert(result.toString());
    }

    public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
    }
};
ServiceDefTarget endpoint = (ServiceDefTarget) service;
endpoint.setServiceEntryPoint("Hello.action");
service.execute("Hello", callback);
```