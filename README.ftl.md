<#assign project_id="gs-serving-mobile-web-content">
This guide walks you through the process of using Spring to serve different views to normal, mobile and tablet devices.

What you'll build
-----------------

You'll build a service that will accept HTTP GET requests at:

    http://localhost:8080/greeting

and respond with a web page displaying a greeting for a normal desktop browser:

    "Hello standard view!"

Mobile phone browsers will see:

    "Hello mobile view!"

and tablets:

    "Hello tablet view!"

What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide jump_ahead='Create a configuration class'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>


<@create_both_builds/>

<@bootstrap_starter_pom_disclaimer/>

By including the Spring Mobile dependency, Spring Boot configures a [`DeviceResolverHandlerInterceptor`] and [`DeviceHandlerMethodArgumentResolver`] automatically. [`DeviceResolverHandlerInterceptor`] examines the `User-Agent` header in the incoming request, and based on the header value, determines whether the request is coming from a desktop browser, a mobile browser, or a tablet browser. The [`DeviceHandlerMethodArgumentResolver`] allows Spring MVC to use the resolved [`Device`] object in a controller method.


<a name="initial"></a>
Create a configuration class
----------------------------

Use the following configuration class to tell Spring where it can find the alternate mobile and tablet views:

    <@snippet path="src/main/java/hello/WebConfiguration.java" prefix="complete"/>

Internally, [`LiteDeviceDelegatingViewResolver`] makes use of the [`Device`] resolved by [`DeviceResolverHandlerInterceptor`] to determine whether to return normal, mobile, or tablet specific views for a request. In this case, [`LiteDeviceDelegatingViewResolver`] is configured to delegate requests to [`ThymeleafViewResolver`]. [`ThymeleafViewResolver`] is a type of [`ViewResolver`], which is used by Spring to perform server-side rendering of HTML.

Autowiring the `SpringTemplateEngine` makes use of the default web configuration in Spring Boot. Declaring the `LiteDeviceDelegatingViewResolver` bean with a name of *thymeleafViewResolver* will override the default `ThymeleafViewResolver` configuration in Spring Boot.


Create a web controller
-----------------------

In Spring's approach to building web sites, HTTP requests are handled by a controller. You can easily identify these requests by the [`@Controller`] annotation. In the following example, the GreetingController handles GET requests for /greeting by returning the name of a [`View`], in this case, "greeting". A [`View`] is responsible for rendering the HTML content:

    <@snippet path="src/main/java/hello/GreetingController.java" prefix="complete"/>

The `@RequestMapping` annotation ensures that HTTP requests to `/greeting` are mapped to the `greeting()` method.

> **Note:** The above example does not specify `GET` vs. `PUT`, `POST`, and so forth, because `@RequestMapping` maps all HTTP operations by default. Use `@RequestMapping(method=GET)` to narrow this mapping.

The implementation of the method body relies on a [view technology][u-view-templates], in this case [Thymeleaf](http://www.thymeleaf.org/doc/html/Thymeleaf-Spring3.html), to perform server-side rendering of the HTML. Thymeleaf parses the `greeting.html` template below and renders the HTML.

    <@snippet path="src/main/resources/templates/greeting.html" prefix="complete"/>

Note the location of the mobile and tablet versions of the greeting view. These were configured earlier in `WebConfiguration` to use the `mobile/` and `tablet/` prefix.

    └── src
        └── main
            └── resources
                └── templates
                    └── greeting.html
                    └── mobile
                        └── greeting.html
                    └── tablet
                        └── greeting.html

The html body differs slightly for each greeting. The tablet version includes the following:

    <p th:text="'Hello tablet view!'" />

Likewise the mobile version of the greeting view includes the following html:

    <p th:text="'Hello mobile view!'" />


Make the application executable
-------------------------------

Although it is possible to package this service as a traditional [WAR][u-war] file for deployment to an external application server, the simpler approach demonstrated in the next section creates a _standalone application_. You package everything in a single, executable JAR file, driven by a good old Java `main()` method. And along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance.

### Create an application class

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the _[Spring application context][u-application-context]_.

The [`@ComponentScan`] annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`] annotation. This directive ensures that Spring finds and registers the `SitePreferenceConfiguration` and `SitePreferenceController` classes, because they are marked with [`@Controller`], which in turn is a kind of [`@Component`] annotation.

The [`@EnableAutoConfiguration`] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`] is configured and registered for you — no `web.xml` necessary! Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

<@build_an_executable_jar_subhead/>

<@build_an_executable_jar_with_both/>

<@run_the_application_with_both module="service"/>

Logging output is displayed. The service should be up and running within a few seconds.


Test the service
----------------

Now that the web site is running, visit http://localhost:8080/greeting, where you see:

    "Hello standard view!"

Using an iOS simulator or Android emulator you will see:

    "Hello mobile view!"

or

    "Hello tablet view!" 


Summary
-------

Congratulations! You have just developed a simple web page that detects the type of device being used by a client and serves different views for each device type.


<@u_war/>
<@u_tomcat/>
<@u_application_context/>
[`@Configuration`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html
[`WebMvcConfigurerAdapter`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurerAdapter.html
[`DeviceResolverHandlerInterceptor`]:http://docs.spring.io/spring-mobile/docs/1.1.x/api/org/springframework/mobile/device/DeviceResolverHandlerInterceptor.html
[`DeviceHandlerMethodArgumentResolver`]:http://docs.spring.io/spring-mobile/docs/1.1.x/api/org/springframework/mobile/device/DeviceHandlerMethodArgumentResolver.html
[`Device`]:http://docs.spring.io/spring-mobile/docs/1.1.x/api/org/springframework/mobile/device/Device.html
[`LiteDeviceDelegatingViewResolver`]:http://docs.spring.io/spring-mobile/docs/1.1.x/api/org/springframework/mobile/device/view/LiteDeviceDelegatingViewResolver.html
[`ThymeleafViewResolver`]:http://www.thymeleaf.org/apidocs/thymeleaf-spring3/1.0.1/org/thymeleaf/spring3/view/ThymeleafViewResolver.html
[`ViewResolver`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/ViewResolver.html
[`@ComponentScan`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/ComponentScan.html
[`@Component`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@Controller`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html
[`View`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/View.html
[`@ResponseBody`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html
[`SpringApplication`]:http://docs.spring.io/spring-boot/docs/0.5.0.M5/api/org/springframework/boot/SpringApplication.html
[`DispatcherServlet`]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
[`@EnableAutoConfiguration`]:http://docs.spring.io/spring-boot/docs/0.5.0.M5/api/org/springframework/boot/autoconfigure/EnableAutoConfiguration.html
