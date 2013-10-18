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
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.8+][gradle] or [Maven 3.0+][mvn]
 - You can also import the code from this guide as well as view the web page directly into [Spring Tool Suite (STS)][gs-sts] and work your way through it from there.

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi
[gs-sts]: /guides/gs/sts


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-serving-mobile-web-content.git`
 - cd into `gs-serving-mobile-web-content/initial`.
 - Jump ahead to [Create a configuration class](#initial).

**When you're finished**, you can check your results against the code in `gs-serving-mobile-web-content/complete`.
[zip]: https://github.com/spring-guides/gs-serving-mobile-web-content/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/spring-guides/gs-serving-mobile-web-content/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/spring-guides/gs-serving-mobile-web-content/blob/master/initial/pom.xml). If you are using [Spring Tool Suite (STS)][gs-sts], you can import the guide directly.

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.spring.io/libs-milestone" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-serving-mobile-web-content'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.spring.io/libs-milestone" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.M5")
    compile("org.springframework.mobile:spring-mobile-device:1.1.0.RELEASE")
    compile("org.thymeleaf:thymeleaf-spring3:2.0.17")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.8'
}
```
    
[gs-sts]: /guides/gs/sts    

> **Note:** This guide is using [Spring Boot](/guides/gs/spring-boot/).

By including the Spring Mobile dependency, Spring Boot configures a [`DeviceResolverHandlerInterceptor`] and [`DeviceHandlerMethodArgumentResolver`] automatically. [`DeviceResolverHandlerInterceptor`] examines the `User-Agent` header in the incoming request, and based on the header value, determines whether the request is coming from a desktop browser, a mobile browser, or a tablet browser. The [`DeviceHandlerMethodArgumentResolver`] allows Spring MVC to use the resolved [`Device`] object in a controller method.


<a name="initial"></a>
Create a configuration class
----------------------------

Use the following configuration class to tell Spring where it can find the alternate mobile and tablet views:

`src/main/java/hello/WebConfiguration.java`
```java
package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.view.LiteDeviceDelegatingViewResolver;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;

@Configuration
public class WebConfiguration {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Bean
    public LiteDeviceDelegatingViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver delegate = new ThymeleafViewResolver();
        delegate.setTemplateEngine(this.templateEngine);
        delegate.setCharacterEncoding("UTF-8");
        LiteDeviceDelegatingViewResolver resolver = new LiteDeviceDelegatingViewResolver(delegate);
        resolver.setMobilePrefix("mobile/");
        resolver.setTabletPrefix("tablet/");
        return resolver;
    }

}
```

Internally, [`LiteDeviceDelegatingViewResolver`] makes use of the [`Device`] resolved by [`DeviceResolverHandlerInterceptor`] to determine whether to return normal, mobile, or tablet specific views for a request. In this case, [`LiteDeviceDelegatingViewResolver`] is configured to delegate requests to [`ThymeleafViewResolver`]. [`ThymeleafViewResolver`] is a type of [`ViewResolver`], which is used by Spring to perform server-side rendering of HTML.

Autowiring the `SpringTemplateEngine` makes use of the default web configuration in Spring Boot. Declaring the `LiteDeviceDelegatingViewResolver` bean with a name of *thymeleafViewResolver* will override the default `ThymeleafViewResolver` configuration in Spring Boot.


Create a web controller
-----------------------

In Spring's approach to building web sites, HTTP requests are handled by a controller. You can easily identify these requests by the [`@Controller`] annotation. In the following example, the GreetingController handles GET requests for /greeting by returning the name of a [`View`], in this case, "greeting". A [`View`] is responsible for rendering the HTML content:

`src/main/java/hello/GreetingController.java`
```java
package hello;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GreetingController {

    @RequestMapping("/greeting")
    public String greeting() {
        return "greeting";
    }

}
```

The `@RequestMapping` annotation ensures that HTTP requests to `/greeting` are mapped to the `greeting()` method.

> **Note:** The above example does not specify `GET` vs. `PUT`, `POST`, and so forth, because `@RequestMapping` maps all HTTP operations by default. Use `@RequestMapping(method=GET)` to narrow this mapping.

The implementation of the method body relies on a [view technology][u-view-templates], in this case [Thymeleaf](http://www.thymeleaf.org/doc/html/Thymeleaf-Spring3.html), to perform server-side rendering of the HTML. Thymeleaf parses the `greeting.html` template below and renders the HTML.

`src/main/resources/templates/greeting.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head> 
    <title>Getting Started: Serving Web Content</title> 
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
    <p th:text="'Hello standard view!'" />
</body>
</html>
```

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

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the _[Spring application context][u-application-context]_.

The [`@ComponentScan`] annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`] annotation. This directive ensures that Spring finds and registers the `SitePreferenceConfiguration` and `SitePreferenceController` classes, because they are marked with [`@Controller`], which in turn is a kind of [`@Component`] annotation.

The [`@EnableAutoConfiguration`] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`] is configured and registered for you — no `web.xml` necessary! Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

### Build an executable JAR

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/spring-guides/gs-serving-mobile-web-content/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.spring.io/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.M4")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of `build.gradle` [right here]((https://github.com/spring-guides/gs-serving-mobile-web-content/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-serving-mobile-web-content-0.1.0.jar
```

If you are using Maven, you can run the JAR by typing:

```sh
$ java -jar target/gs-serving-mobile-web-content-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/spring-projects/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the service
-------------------
If you are using Gradle, you can run your service at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-serving-mobile-web-content-0.1.0.jar
```

> **Note:** If you are using Maven, you can run your service by typing `mvn clean package && java -jar target/gs-serving-mobile-web-content-0.1.0.jar`.


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


[u-war]: /understanding/WAR
[u-tomcat]: /understanding/Tomcat
[u-application-context]: /understanding/application-context
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
