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

__conf4j__ is a library that allows building object-oriented, type safe configurations.

_Configuration_ is represented as an interface or abstract class optionally annotated with _conf4j_ annotations.

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

Then a configuration type instance must be created and attached to a _value source_ using a _configuration factory_.

```java
ConfigurationValuesSource valuesSource = new PropertiesConfigurationValuesSource("configuration.properties");
ConfigurationFactory configurationFactory = new JdkProxyStaticConfigurationFactory();
ConnectionConfiguration configuration = configurationFactory.createConfiguration(ConnectionConfiguration.class, valuesSource);
```

Once the configuration instance is created you can access the configuration values via getters:

```java
String url = configuration.getUrl();
int connectionTimeout = configuration.getTimeouts().getConnectionTimeout();
```

For more details how to use _conf4j_ please read [Conf4j User's Guide](USERS-GUIDE.md)
and check [cmm-examples](conf4j-examples) directory.

## Contributing

We accept pull request via _GitHub_. There are some guidelines which will make applying PRs easier for us:

* No tabs. Please use spaces for indentation.
* Respect the code style.
* Create minimal diffs - disable on save actions like reformat source code or organize imports.
  If you feel the source code should be reformatted create a separate PR for this change.
* Provide _JUnit_ tests for your changes and make sure your changes don't break any existing tests by running
  `mvn clean test`.

See [CONTRIBUTING](CONTRIBUTING.md) document for more details.

## License

Copyright 2017 Sabre GLBL Inc.

Code is under the [MIT license](LICENSE.txt).
