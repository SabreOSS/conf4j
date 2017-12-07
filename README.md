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

## _conf4j_ - Type-safe Configuration Library for Java

[![Build Status](https://travis-ci.org/SabreOSS/conf4j.svg?branch=master)](https://travis-ci.org/SabreOSS/conf4j)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sabre.oss.conf4j/conf4j-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sabre.oss.conf4j/conf4j-api)

__conf4j__ is a library which allows accessing configuration data in object-oriented, type-safe manner.

In _conf4j_ configuration is represented as an interface or abstract class optionally annotated with _conf4j_ annotations.

```java
@Key("connection")
public interface ConnectionConfiguration {
   String getUrl();

   String getUser();

   String getPassword();

   @Default("60")
   int getConnectionTimeout();

   @Default("30")
   int getReadTimeout();
}
```

Then a configuration instance is created and bound to the _value source_ by the _configuration factory_.

```java
ConfigurationSource source = new PropertiesConfigurationSource("configuration.properties");
ConfigurationFactory factory = new JdkProxyStaticConfigurationFactory();
ConnectionConfiguration configuration = factory.createConfiguration(ConnectionConfiguration.class, source);
```

Once the configuration instance is created you can access the configuration values via getters:

```java
String url =  configuration.getUrl();
int connectionTimeout = configuration.getConnectionTimeout();
```

Example _configuration.properties_ is as follows:

```properties
connection.url=https://github.com/SabreOss/conf4j
connection.user=john
connection.password=secret
connection.connectionTimeout=45
```

For more information how to use _conf4j_ please read [Conf4j User's Guide](USERS-GUIDE.md)
and check [conf4j-examples](conf4j-examples) directory.

## Contributing

We accept pull request via _GitHub_. Here are some guidelines which will make applying PRs easier for us:

* No tabs. Please use spaces for indentation.
* Respect the code style.
* Create minimal diffs - disable on save actions like reformat source code or organize imports.
  If you feel the source code should be reformatted create a separate PR for this change.
* Provide _JUnit_ tests for your changes and make sure they don't break anything by running
  `mvn clean verify`.

See [CONTRIBUTING](CONTRIBUTING.md) document for more details.

## License

Copyright 2017 Sabre GLBL Inc.

Code is under the [MIT license](LICENSE).
