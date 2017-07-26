<!--
  MIT License

  Copyright 2017 Sabre GLBL Inc.

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 -->

# Conf4j User's Guide

* [Overview](#overview)
* [Getting Started](#getting-started)
* [Configuration Factory](#configuration-factory)
* [Configuration Keys](#configuration-keys)
* [Type Converters](#type-converters)
* [Configuration Types with Generics](#configuration-types-with-generics)
* [Spring Framework Integration](#spring-framework-integration)

## Overview

__conf4j__ is a library that allows building object-oriented, type safe configurations.

_Configuration_ is represented as an interface or abstract class optionally annotated with _conf4j_ annotations.

#### Features

* Simple, intuitive, annotation driven _API_ for defining configuration types.
* All configuration properties are statically typed and validated.
* Out of the box support for all primitive types and they wrappers as well as `List` and `Map`.
* Integrated with [Spring Framework](https://projects.spring.io/spring-framework) and
  and [Spring Boot](http://projects.spring.io/spring-boot).
* High level _API_ for accessing configuration values.
* Minimum set of dependencies: [SLF4J](http://slf4j.org) and [commons-lang](https://commons.apache.org/proper/commons-lang).

## Getting Started

To start playing with _conf4j_ just add the dependency to _com.sabre.oss:conf4j-core_
or _com.sabre.oss:conf4j-spring_ (if you wish to integrate the library with _Spring Framework_).

_Maven_
```xml
<dependency>
  <groupId>com.sabre.oss</groupId>
  <artifactId>conf4j-core</artifactId>
  <version>${conf4j.version}</version>
</dependency>
```

_Gradle_
```groovy
dependencies {
  compile "com.sabre.oss:conf4j-core:$conf4jVersion"
}
```

Of course make sure `conf4j.version` variable (for _Maven_) or `conf4jVersion` (for _Gradle_) is set to the proper
_conf4j_ version.

In _conf4j_, a configuration is expressed as a public interface or public abstract class annotated with _conf4j_ annotations.

```java
public interface ConnectionConfiguration {
   @Key
   String getUrl();

   TimeoutConfiguration getTimeouts();
}

public interface TimeoutConfiguration {
    @Key
    @DefaultValue("60")
    int getConnectionTimeout();

    @Key
    @DefaultValue("30")
    int getReadTimeout();
}
```

The configuration consists of a set of properties (which follows _JavaBeans_ naming conventions).
Each property must be an `public`, `abstract`, _parameter-less_ method which return type cannot be `void`.
The return type can be any type supported by the `TypeConverter`, another configuration type
or `List` of configuration types.

Every configuration property has a set of _configuration keys_ assigned which are determined
based on _property name_, `@Key`, `@FallbackKey`, `@IgnorePrefix` and `@IgnoreKey` annotations.
The rules that govern how the _configuration key set_ is constructed are covered in the
[Configuration Keys](#configuration-keys) section.

When a value related to a configuration property is required, it is retrieved from the `ConfigurationValuesSource`.
Each key associated with the property is checked in the sequence until the value associated with the key is found
in the _values source_. It is also possible to specify a default value for the property via the
`@DefaultValue` annotation and `@DefaultsAnnotation` meta-annotations. See _javadoc_ for details.

The configuration type is like a template. Before you begin accessing the configuration, a configuration instance
must first be created and bound to the _values source_. This can be done via `ConfigurationFactory.createConfiguration()`
as shown below.

```java
// Create values source from the property file.
ConfigurationValuesSource valuesSource = new PropertiesConfigurationValuesSource("configuration.properties");

// Create configuration factory - it is thread safe.
ConfigurationFactory configurationFactory = new JdkProxyStaticConfigurationFactory();

// Create configuration instance and bind it to the values source.
ConnectionConfiguration configuration = configurationFactory.createConfiguration(ConnectionConfiguration.class, valuesSource);

// Now you can access configuration properties via configuration object.
String url = configuration.getUrl();
int connectionTimeout = configuration.getTimeouts().getConnectionTimeout();
```

The `ConfigurationValuesSource` allows accessing a configuration value associated with a key. Both key and value
are represented as string. Of course, very often, the configuration property type is not `java.lang.String`
so the value must be converted into the appropriate type.
_Type converters_ which implement `TypeConverter` interface are responsible for such conversion.

The _conf4j_ library provides type converters for all primitive types (like `boolean`, `int`, `double`),
primitive wrapper types (like `Boolean`, `Integer`, `Double`), _enumerations_ and many other types which are frequently
used (like `BigDecimal`). More complex types are also supported - there is a dedicated converter for `List<E>` and `Map<K, V>`
which is able to convert even very complex types like `Map<String, Map<Integer, List<Double>>>`. Because lists and maps
are quite complex, the data must be encoded to be represented as a string. Currently `JsonLikeTypeConverter` is used
as a default implementation. It encodes `List` and `Map` as _JSON_, but by default _compact mode_ is activated to make
the encoded string more user friendly.

For example `List<String>` in compact mode is represented as `[one,two,tree]` in JSON mode as `["one","two","three"]`.
Map<String, List<String>> in compact mode: `{key1:[val11,val12],key2:[val21,val22]}` in JSON: `{"key1":["val11","val12"],"key2":["val21","val22"]}`.
Because quotation mark `"` must be escaped in _java_, JSON format is very inconvenient for specifying default value in `@DefaultValue`.

Please consult javadoc for `JsonLikeTypeConverter` class to get more information about the format.

Of course there is possibility to provide custom implementation of `TypeConverter`.
For details, see the [Type Converters](#type-converters) section.

_conf4j_ provides out of the box integration with [Spring Framework](https://projects.spring.io/spring-framework)
and [Spring Boot](http://projects.spring.io/spring-boot) application frameworks.

First of all add dependency to `com.sabre.oss::conf4j-spring` module to your project.

```xml
<dependency>
  <groupId>com.sabre.oss</groupId>
  <artifactId>conf4j-spring</artifactId>
  <version>${conf4j.version}</version>
</dependency>
```

Then annotate the root configuration with _Spring Framework_ `@Component` annotation.
```java
@Component
public interface ConnectionConfiguration {
   @Key
   String getUrl();

   @Key
   String getUser();
}
```

Assuming you would like to use annotation-based configuration, use `@EnableConf4` in your configuration class
and search for configurations with `@ConfigurationsScan` as shown below.

```java
@EnableConf4j
@ConfigurationScan
public class Application {
    @Autowired
    private ConnectionConfiguration connectionConfiguration;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class)) {
            Application application = context.getBean(Application.class);
            application.run();
        }
    }

    private void run() {
       String url = connectionConfiguration.getUrl();
       // ...
    }

    @Key("connection")
    public interface ConnectionConfiguration {
       @Key
       String getUrl();

       @Key("user")
       String getUserName();
    }
}
```

For details, see [Spring Framework Integration](#spring-framework-integration) section.

## Configuration Factory

The `ConfigurationFactory` interface specifies how the configuration type is instantiated and bound
to the `ConfigurationValuesSource`. It takes a configuration type and _values source_ and creates a configuration instance.

```java
ConnectionConfiguration configuration = factory.createConfiguration(ConnectionConfiguration.class, valuesSource);
```

How a configuration instance is generated depends on the configuration factory implementation.
There are two flavors of the `ConfigurationFactory`: _static_ and _dynamic_ configuration factories.

_Static configuration factory_ creates _static_/_frozen_ configuration instances which retrieves values from
`ConfigurationValuesSource` only during the instance-creation phase. The configuration values are then stored
in the configuration instance and never change in the future. Access to _static_ configuration properties is very fast,
it's just a getter invocation.

_Static_ configuration classes implements the `java.io.Serializable` interface which allows a configuration instance
to be serialized, as long as all configuration property types are also serializable.

`Dynamic configuration factory` creates a _dynamic_ configuration instance which hits `ConfigurationValuesSource`
every time the configuration property method is invoked. It is an ideal choice when values in _values source_
changes over time and a configuration should reflect those changes.
Bear in mind there is an overhead associated with a configuration property access. It requires getting configuration
value from the source and converting it the proper type.

_conf4j_ provides three configuration factory families:

* `JdkProxyStaticConfigurationFactory` and `JdkProxyDynamicConfigurationFactory` use _jdk proxy_
  `java.lang.reflect.Proxy` for creating configuration instances. These factories support configuration types
  which are interfaces, abstract classes are not supported due to _jdk proxy_ limitations.
* `JavassistStaticConfigurationFactory` and `JavassistDynamicConfigurationFactory` use
  [Javassist](http://jboss-javassist.github.io/javassist) to generate configuration implementations on the fly.
* `CglibStaticConfigurationFactory` and `CglibDynamicConfigurationFactory` use [CGLIB](https://github.com/cglib/cglib/wiki)
  (to be precise - CGLIB repackaged version provided by _Spring Framework_). These factories are available only
  whey you use _conf4j_ integration with _Spring Framework_.

## Configuration Keys

Every configuration property has an ordered set of _configuration keys_ assigned. A property can be associated
with multiple keys, and multiple properties can be associated to the same keys. When a configuration value is required,
the keys from the _key set_ is submitted to `ConfigurationValuesSource` in sequence until the value associated
with the key is found.

_Key set_ is constructed based on _conf4j_ annotations.

`@Key` annotation specifies the key for a value property. If `@Key` annotation is missing, the _property name_ is used.

```java
public interface ConnectionConfiguration {
   @Key
   String getUrl();

   @Key("user")
   String getUserName();
}
```

_ConnectionConfiguration_ has two configuration properties: _url_ and _userName_.
The key set for the _url_ property consists of one key _url_.
When the key name is not specified by the `@Key` annotation, the property name is used. `@Key` annotation without
parameters is optional (as long as default, _non-strict_ configuration model provider is used) and property name is used
as a key in such case. But using it, indicates explicitly a method is the configuration property and is a good practice.

The _userName_ property has one key _user_ assigned and its name is explicitly specified by the `@Key` annotation.

When applied to the configuration type or property which returns sub-configuration or list of sub-configurations,
an `@Key` annotation can define a prefix for the configuration keys within the hierarchy.

For the example below, _url_ property has two keys assigned: _connection.url_ and _alternateConnection.url_.
Please note the prefix and key are joined by `.` (dot) character which is a delimiter used to join configuration key components.

```java
@Key({"connection", "alternateConnection"})
public interface ConnectionConfiguration {
   @Key
   String getUrl();

   @Key("user")
   String getUserName();
}
```

`@Key` normally specifies just one key or prefix; however, it allows you to define multiple keys or prefixes.
If the key prefix value is not specified, the uncapitalized type name or property name is used
(when the `@Key` is applied to the _sub-configuration_).

Here is a more complex scenario with configuration and sub-configurations involved.

```java
@Key({"connection", "alternateConnection"})
public interface ConnectionConfiguration {
   @Key
   String getUrl();

   @Key("user")
   String getUserName();

   TimeoutConfiguration getTimeout();

   @Key("default")
   TimeoutConfiguration getDefaultTimeout();

   @Key("other")
   List<TimeoutConfiguration> getOtherTimeouts();
}

@Key("timeout")
public interface TimeoutConfiguration {
   @Key("connect")
   Integer getConnectTimeout();

   @Key("read")
   Integer getReadTimeout();
}

```

Let's create a configuration instance `configuration`.

```java
ConnectionConfiguration configuration = factory.createConfiguration(ConnectionConfiguration.class, source);
```

For the following invocation:
```java
Integer connect = configuration.getTimeout().getConnectTimeout();
```
the key set for `getConnectionTimeout()` is: _connection.timeout.connect_, _alternateConnection.timeout.connect_.

_connection_ and _alternateConnection_ key prefixes are defined by `@Key` applied on `ConfigurationConfiguration`,
_timeout_ key prefix from `@Key` applied on `@TimeoutConfiguration`.

For the following invocation:
```java
Integer connect = configuration.getDefaultTimeout().getConnectTimeout();
```
the key set for `getConnectionTimeout()` is: _connection**.default**.timeout.connect_, _alternateConnection**.default**.timeout.connect_
because `getDefaultTimeout()`  is annotated with `@Key("default")`

A list of sub-configurations are handled a little differently. Because the list contains multiple elements, there must
be a way to distinguish between keys assigned to different elements. Every element in the list has an index which
becomes part of the key.

For the following invocation:
```java
Integer connect = configuration.getOtherTimeouts().get(0).getConnectTimeout();
```
the key set for `getConnectionTimeout()` is:
_connection**.other[0]**.timeout.connect_, _alternateConnection**.other[0]**.timeout.connect_,
_connection.other.timeout.connect_, _alternateConnection.other.timeout.connect_.

The first two keys have `[0]` appended to the `other` key component. _conf4j_ uses the `[index]` convention
(where _index_ is an element index in the list) to indicate a key is associated with a list element.

Surprisingly the key set contains _connection.other.timeout.connect_ and _alternateConnection.other.timeout.connect_.
They are added for each element's key set as a fallback. These keys are used only when there is no value associated
with indexed keys in the _values source_.

For the configuration property which returns a list of sub-configuration there is a need to provide the size of the list.
There is an additional key set associated, which is created by appending the _.size_ suffix.
For `configuration.getOtherTimeouts()` the key set is: _connection.other.size_, _alternateConnection.other.size_.

_conf4j_ provides more annotations like `@FallbackKey` or `@IgnoreKey` which influence the way a _key set_ is generated.
Please consult the _javadoc_ for details.

## Type Converters

_Type converter_ allows converting a string value into the appropriate type like `int`, `decimal` or even `List<Integer>`.
Type converter must implement a `TypeConverter` interface and provide the method for converting a string into a value
of a proper type as well as a method for converting the value back to a string representation.

`TypeConverter` is usually provided for simple types like `int` but it may also convert complex objects and graphs,
e.g., the objects annotated with _JAXB_ annotations.

The converter must be _thread safe_ and _stateless_ because it's accessed from multiple threads and different contexts.
Ideally it should be _symmetric_ so the result of _value_ conversion to string, and then the string back to the value,
produces the same value.

_conf4j_ provides converters for commonly used types like `int`, `long`, `boolean`, `double`, _enumerations_, `String`
but also converts to `List<E>` and `Map<K, V>` . The former converters are generic and support any `E, K, V` types
as long as converters for `E, K, V` are available.
Even exotic types are supported: `Map<List<String>, List<Map<Integer, Double>>`.

`TypeConverter` specifies the format of the string representation of a type. For some types like `int` the natural
string representation is already defined, e.g., by `toString()` method. On the other hand, you may wish to change
the format slightly. For example, `EscapingStringTypeConverter` replaces new line separator with `\n`
character or tab with `\t` - in general, it uses the same rules for escaping as _Java_.

`ConfigurationFactory` is pre-configured with wide range of converters (it may be can be altered by `setTypeConverter()`).
In case there is a need to use a type converter which is not known by `ConfigurationFactory` or the type converter
registered in the factory is not appropriate (e.g. you would like to convert `Integer` to hexadecimal value)
'@Converter` annotation can be used as shown below:

```java
public interface DateRange {
  @Key
  @Converter(DateTimeConverter.class)
  DateTime getStart();

  @Key
  @Converter(DateTimeConverter.class)
  DateTime getEnd();
}
```

`TypeConverter` implementation pointed by `@Converter` must provide public, parameterless constructor and it is instantiated
on runtime via reflection. Type converter has to be _thread-safe_ and _stateless_. Because many instances of the converter are created,
make sure creating new instance is fast and created object doesn't occupy much memory.

In general favor setting up `ConfigurationFactory` with the appropriate converters over using `@Converter` annotation.

## Configuration Types with Generics

_conf4j_ configuration types supports generics. It simplifies creating configurations which shares same behaviour.

Following example demonstrates how to use generics to create a base configuration type `ValidatorConfiguration`
with simple properties _name_, _enabled_ and customizable property _constraints_.

```java
@AbstractConfiguration
@Key
public interface ValidatorConfiguration<C> {
    @Key
    String getName();

    @Key
    boolean isEnabled();

    C getConstraints();
}

public interface IntegerValidatorConfiguration extends ValidatorConfiguration<IntegerConstraints> {
}

public interface IntegerConstraints {
    @Key
    Integer getMin();

    @Key
    Integer getMax();
}

public interface StringValidatorConfiguration extends ValidatorConfiguration<StringConstraints> {
}

public interface StringConstraints  {
    @Key
    Pattern getPattern();
}
```

Because `ValidatorConfiguration` serves as a template (actual sub-configuration type is not known), it has been marked as
an _abstract_ using `@AbstractConfiguration` annotation.

There are two concrete configurations `IntegerValidatorConfiguration` and `StringValidatorConfiguration`
which extends `ValidatorConfiguration` and specifies the constraint type.

Generics can be used not only for sub-configurations properties but for value properties too.
`MinMax` abstract configuration type (see example below) has two _min_ and _max_ properties of generic type _T_.

`IntegerConstraint` and `SubStringConstraint` extends `MinMax` and specifies the type of _min_ and _max_ as _Integer_.

```java
@AbstractConfiguration
@Key
public interface MinMax<T extends Comparable<T>> {
    @Key
    T getMin();

    @Key
    T getMax();
}

public interface IntegerConstraint extends MinMax<Integer> {
}

public interface StringConstraint extends MinMax<String> {
    @Key
    Pattern getPattern();
}
```

More unusual generic usages for value properties are also possible.

```java
@AbstractConfiguration
@Key
public interface AbstractExoticConfiguration<K, V> {
    @Key
    List<Map<K, List<V>>> getProperty();
}

public interface SomeConfiguration extends AbstractExoticConfiguration<String, Integer> {
}

```


## Spring Framework Integration

_conf4j_ provides out of the box integration with [Spring Framework](https://projects.spring.io/spring-framework)
First of all add dependency to `com.sabre.oss::conf4j-spring` module to your project.

_Maven_
```xml
<dependency>
  <groupId>com.sabre.oss</groupId>
  <artifactId>conf4j-spring</artifactId>
  <version>${conf4j.version}</version>
</dependency>
```

_Gradle_
```groovy
dependencies {
  compile "com.sabre.oss:conf4j-spring:$conf4jVersion"
}
```

_conf4j_ supports XML schema-based as well as annotation driven configurations and both types can be
used simultaneously.

### XML Schema-based Configuration

_conf4j_ provides custom configuration schema `http://www.sabre.com/schema/oss/conf4j` which exposes two custom tags:
`<conf4j:configure/>`, `<conf4j:configuration/>` and `<conf4j:configuration-scan/>`.

`<conf4j:configure/>` is used for activating _conf4j_ integration with _Spring Framework_. It registers several infrastructure
beans (like `ConfigurationFactory`, `ConfigurationValuesSource` or `TypeConverter`) and post processors. Such beans are used
by _conf4j_ for registering configuration types as beans, creating configuration instances and binding the values source.

`<conf4j:configuration/>` registers in the context a bean for the configuration type.
This tag expects one attribute `class` which specifies fully qualified name of the configuration type. It is also
possible to specify the bean name using `id` attribute (by default fully qualified class name is used).

```xml
<conf4j:configuration class="com.your.organization.configuration.package.ConnectionConfiguration"/>
```

`<conf4:configuration-scan>` is very similar to `<context:component-scan/>` provided by _Spring Framework_.
It searches for the configuration types in the package (and all its sub-packages) specified by `base-package` attribute
and register them as a beans to the context. To make a _conf4j_ configuration type discoverable, it must be
annotated (or meta-annotated) by `@Component` annotation provided by _Spring Framework_. There also an option
use your own annotation (or annotations), just specify it by `configuration-annotations` attribute.

```xml
<conf4j:configuration-scan base-package="com.your.organization.configuration.package"
                        configuration-annotations="com.your.organization.ConfigurationAnnotation"/>
```

`<conf4:configuration-scan>` supports filtering via `include-filter` and `exclude-filter` elements exactly the same way
as `<context:component-scan/>`.

The example below shows how to use `<conf4j:configure/>` and `<conf4:configuration-scan>` to activate _conf4j_
and register all configuration types from _com.your.organization.configuration.package_ in the the context.

```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:conf4j="http://www.sabre.com/schema/oss/conf4j"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.sabre.com/schema/oss/conf4j http://www.sabre.com/schema/oss/conf4j/conf4j.xsd"
       default-lazy-init="true">

    <!-- Enable conf4j -->
    <conf4j:configure/>

    <!-- Find configuration types from the specified package and register in the context. -->
    <conf4j:configuration-scan base-package="com.your.organization.configuration.package"/>

</beans>
```

As mentioned earlier, `<conf4j:configure/>` registers several beans with into the context. Each bean has pre-defined
name which starts with `com.sabre.oss.conf4j.` prefix.

The bean `com.sabre.oss.conf4j.configurationValuesSource` is used for providing configuration values and must implement `ConfigurationValuesSource`
interface. _conf4j_ registers `PropertySourceConfigurationValuesSource` by default. This values sources integrates with
`Environment` and all `PropertySourcesPlaceholderConfigurer`s registered in the context
(e.g. by `<context:property-placeholder .../>` or `@PropertySource`).

If you would like to provide your own implementation of `ConfigurationValuesSource` (e.g. to fetch configuration values
from the database) just declare own bean or an alias with `com.sabre.oss.conf4j.configurationValuesSource` name.

```xml
<bean id="com.sabre.oss.conf4j.configurationValuesSource" class="com.your.organization.CustomConfigurationValuesSource">
  <!-- ... -->
</bean>
```

In general, all _conf4j_ specific bean can be overridden this way.

The bean `com.sabre.oss.conf4j.typeConverter` is uses for converting string representation of the values to appropriate type.
By default _conf4j_ registers converter provided by `DefaultTypeConverters.getDefaultTypeConverter()`. This converter
is able to convert all primitive types (and they object counterparts), enumerations any combination of List and Map
e.g. `Map<String, List<Integer>>`.

The bean `com.sabre.oss.conf4j.configurationFactory` is used for creating configuration instances. _conf4j_ registers
`JavassistDynamicConfigurationFactory` or, if its not on the classpath, it fallbacks to `CglibDynamicConfigurationFactory`.
_jaavssist_ based implementation is preferred because it has significantly better performance. But if you don't like
an additional dependency on the classpath, CGLIB based implementation is used (to be precise, repackaged CGLIB version
available with _Spring Framework_ is used).

`JavassistDynamicConfigurationFactory` (and its static counterpart `JavassistStaticConfigurationFactory`) implementation
is provided in the `com.sabre.oss::conf4j-javassist` module. If you would like to use it instead of
`CglibDynamicConfigurationFactory`, make this dependency is included in your project.

_Maven_
```xml
<dependency>
  <groupId>com.sabre.oss</groupId>
  <artifactId>conf4j-javassist</artifactId>
  <version>${conf4j.version}</version>
</dependency>
```

_Gradle_
```groovy
dependencies {
  compile "com.sabre.oss:conf4j-javassist:$conf4jVersion"
}
```

### Annotation-based Configuration

_conf4_ provides following annotations which can be used with _Spring Framework_ and _Spring Boot_:
`@EnableConf4j`, `@ConfigurationScan`, `@ConfigurationType`.

The `@EnableConf4j` enables _conf4j_ integration and works as `<conf4j:configure/>` tag.

The `@ConfigurationScan` scans for the configuration types and its functionality is very similar to `<conf4j:configuration-scan>` tag.

The `@ConfigurationType` registers in the context a bean for the configuration type and its functionality is very similar to `<conf4j:configuration>` tag.

_conf4j_ annotations can be used in _Spring Framework_ configurations classes - classes annotated, or meta-annotated with `@Configuration`.

Following example shows how to use `@ConfigurationScan` to find all configuration classes in the package (and sub-packages).

```java
@Configuration
@EnableConf4j
@ConfigurationScan(basePackageClasses = ConnectionConfiguration.class)
public class SampleConfiguration {
}

@Component
public interface ConnectionConfiguration {
   @Key
   String getUrl();
   // ...
}
```

If you prefer explicitly listing the configuration types use `@ConfigurationType` as shown below. In such case using
`@Component` is redundant and can be skipped.

```java
@Configuration
@EnableConf4j
@ConfigurationType(ConnectionConfiguration.class)
@ConfigurationType(UserConfiguration.class)
public class SampleConfiguration {
}

public interface ConnectionConfiguration {
   @Key
   String getUrl();
   // ...
}

public interface UserConfiguration {
   @Key
   String getLogin();
   // ...
}
```

## Spring Boot Integration

_conf4j_ integrates with _Spring Boot_ via auto-configuration mechanism based on `org.springframework.boot.autoconfigure.EnableAutoConfiguration`.
To activate it, just add dependency to `com.sabre.oss:conf4j-spring-boot` module:

_Maven_
```xml
<dependency>
  <groupId>com.sabre.oss</groupId>
  <artifactId>conf4j-spring-boot</artifactId>
  <version>${conf4j.version}</version>
</dependency>
```

_Gradle_
```groovy
dependencies {
  compile "com.sabre.oss:conf4j-javassist:$conf4jVersion"
}
```

_Spring Boot_ finds _conf4j_ during the bootstrap phase and activates it - there is no need to use `@EnableConf4j`
nor `<conf4j:configure/>` anymore.

```java
@SpringBootApplication
@ConfigurationScan
@PropertySource("classpath:application.properties")
public class SimpleBootApplication implements CommandLineRunner {
    @Autowired
    private ConnectionConfiguration connectionConfiguration;

    public static void main(String[] args) {
        SpringApplication.run(SimpleBootApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("url: " + connectionConfiguration.getUrl());
        //...
    }

    @Component
    @Key("connection")
    public interface ConnectionConfiguration {
        @Key
        String getUrl();
        // ...
    }
}
```

If you want to use _javassist_ based configuration factories, don't forget to add dependency to `com.sabre.oss:conf4j-javassist`.
