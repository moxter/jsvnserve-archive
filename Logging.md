# Introduction #

jSVNServe uses for logging the [Simple Logging Facade for Java](http://www.slf4j.org/). Depending of the used logging implementation within a project, some special bindings must be defined at deployment time.

# Logging with Apache log4j #
To use [Apache log4j](http://logging.apache.org/log4j/) for logging following maven dependencies must be defined within the pom:
```
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>jcl-over-slf4j</artifactId>
    <version>1.5.6</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>jul-to-slf4j</artifactId>
    <version>1.5.6</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.5.6</version>
</dependency>
```

# Automatic Tests #
For the automatic tests [Apache log4j](http://logging.apache.org/log4j/) is used.