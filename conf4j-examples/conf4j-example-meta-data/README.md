This example demonstrates _conf4j custom meta-data_ defined via `@Meta` annotation.

`@SourceFile` annotation which is meta-annotated by `@Meta` allows specifying
the source property file which is a source of configuration values.

`SourceFileAwareConfigurationValuesSource` recognizes _file_ meta-data attribute and based on
its value loads value from the appropriate properties file.
