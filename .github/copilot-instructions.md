# GitHub Copilot Instructions for OPS4J Pax JMS

## Project Overview

This is **OPS4J Pax JMS**, a lightweight bridge between Java Message Service (JMS) and OSGi that uses Configuration Admin, Declarative Services, and the whiteboard pattern. The project provides OSGi-based JMS connection factory factories for various JMS providers.

**Version 2.0+** uses Jakarta Messaging (jakarta.jms) and requires JDK 11+. For javax.jms support with JDK 8, use version 1.x.

## Project Structure

This is a multi-module Maven project with the following modules:

- **pax-jms-api**: Core API and service interfaces
- **pax-jms-config**: Configuration Admin integration
- **pax-jms-activemq**: ActiveMQ implementation
- **pax-jms-artemis**: Apache Artemis implementation
- **pax-jms-ibmmq**: IBM MQ implementation
- **pax-jms-oracleaq**: Oracle AQ implementation
- **pax-jms-pool-pooledjms**: Pooled JMS connection factory
- **pax-jms-pool-narayana**: Narayana transaction manager integration
- **pax-jms-pool-transx**: TransX pooling implementation
- **pax-jms-features**: Karaf feature definitions
- **pax-jms-itest**: Integration tests

## Code Standards

### Package Structure
- All code should be in the `org.ops4j.pax.jms` package hierarchy
- Each module has its own subpackage (e.g., `org.ops4j.pax.jms.activemq`)

### License Headers
All Java files must include the Apache License 2.0 header:
```java
/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

### OSGi Patterns
- Use OSGi Declarative Services (DS) annotations for component definitions
- Follow the whiteboard pattern for service registration
- Implement proper OSGi bundle activators when needed
- Use OSGi Configuration Admin for dynamic configuration

### Maven/Build
- Target JDK 11 compatibility
- Use Maven Bundle Plugin for OSGi bundle generation
- Follow the version properties defined in the parent POM
- Ensure proper OSGi metadata in MANIFEST.MF

## Key Technologies

- **OSGi Framework**: Felix 5.x
- **JMS 3.x**: Jakarta Messaging (jakarta.jms)
- **JMS Providers**: ActiveMQ 5.18.x (with activemq-client-jakarta), Artemis 3.0.x, IBM MQ 9.3+, Oracle AQ 21c+
- **Connection Pooling**: pooled-jms 3.x, Narayana, TransX
- **Testing**: JUnit 4.13.x, Pax Exam 4.13.x
- **Logging**: SLF4J 1.7.x

## Common Patterns

### Connection Factory Factory
Each JMS provider module should implement the `ConnectionFactoryFactory` interface:
```java
public interface ConnectionFactoryFactory {
    ConnectionFactory createConnectionFactory(Map<String, Object> props) throws JMSRuntimeException;
    XAConnectionFactory createXAConnectionFactory(Map<String, Object> props) throws JMSRuntimeException;
}
```

### Configuration Properties
Standard JMS configuration properties:
- `name`: Connection factory name
- `type`: Connection factory type
- `protocol`: JMS protocol to use
- `user`: Username for authentication
- `password`: Password for authentication

### OSGi Service Registration
Use DS annotations for service components:
```java
@Component(service = ConnectionFactoryFactory.class)
public class ExampleConnectionFactoryFactory implements ConnectionFactoryFactory {
    // Implementation
}
```

## Dependencies

### Critical Version Constraints
- **ActiveMQ**: 5.18.x with activemq-client-jakarta (Jakarta JMS support, requires JDK 11+)
- **Artemis**: 3.0.x (Jakarta JMS support, requires JDK 11+)
- **Guava**: Compatible with Artemis 3.0.x
- **pooled-jms**: 3.x (Jakarta JMS support, requires JDK 11+)
- **Karaf**: 4.4.x (Jakarta support)

Always check the parent POM for current version properties before adding or updating dependencies.

## Testing

- Unit tests use JUnit 4
- Integration tests use Pax Exam
- Test resources include log4j2-test.properties for logging configuration
- Connection tests should verify both normal and XA connection factories

## Build Commands

```bash
# Build entire project
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build specific module
cd pax-jms-activemq && mvn clean install

# Run integration tests
cd pax-jms-itest && mvn clean verify
```

## Code Quality

- Follow Checkstyle rules defined in `pax-jms-checks.xml`
- Use suppressions from `pax-jms-checks-suppressions.xml` when appropriate
- Ensure proper JavaDoc for public APIs
- Write meaningful commit messages

## When Implementing New Features

1. Add necessary dependencies to the appropriate module POM
2. Implement service interfaces from pax-jms-api
3. Create OSGi DS components with proper annotations
4. Add unit tests in `src/test/java`
5. Update feature.xml if adding new OSGi bundles
6. Ensure JDK 11 compatibility
7. Add proper license headers
8. Run Checkstyle validation

## When Adding a New JMS Provider

1. Create a new module: `pax-jms-[provider]`
2. Implement `ConnectionFactoryFactory`
3. Create an `Activator` if needed
4. Add provider-specific connection factory implementation
5. Include unit and integration tests
6. Add module to parent POM
7. Update `pax-jms-features/feature.xml`

## Documentation

- Project wiki: https://ops4j1.jira.com/wiki/spaces/PAXJMS/overview
- Issue tracker: https://github.com/ops4j/org.ops4j.pax.jms/issues
- Maven Central: Check latest releases at maven-badges.herokuapp.com
