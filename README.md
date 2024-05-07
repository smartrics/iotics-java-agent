_This project is now deprecated; see [iotics4j](https://github.com/smartrics/iotics4j) _

# iotics-java-agent

IOTICS agent framework for Java

## Protobuffers and gRPC services

The IOTICS API is added as git submodule in `iotics-api` and the proto files symlinked from `src/main/proto`.

The relevant Google's proto files have been copied manually.

## Build

```shell
mvn clean
mvn package
```

If used in other projects run `mvn install` to install it in your own local repository

To deploy:

```shell
mvn clean deploy -Prelease
```
