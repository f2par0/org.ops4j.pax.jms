org.ops4j.pax.jms
=================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.ops4j.pax/jms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.ops4j.pax/jms)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://ops4j1.jira.com/wiki/display/ops4j/Licensing)

Pax JMS provides a lightweight bridge between Java Message Service (JMS) and OSGi using Declarative Services and the whiteboard pattern
## Version Information

- **Version 2.0+**: Uses Jakarta Messaging (jakarta.jms) and requires JDK 11+
  - ActiveMQ 5.18.x with activemq-client-jakarta
  - Artemis 3.0.x
  - pooled-jms 3.x
  - Karaf 4.4.x

- **Version 1.x**: Uses javax.jms and supports JDK 8+
  - For projects that cannot migrate to JDK 11

## Migration from 1.x to 2.0

When migrating from version 1.x to 2.0, you need to:

1. **Update Java Version**: Upgrade to JDK 11 or later
2. **Update Import Statements**: Change all `javax.jms.*` imports to `jakarta.jms.*`
3. **Update OSGi Filters**: Change references from `javax.jms.ConnectionFactory` to `jakarta.jms.ConnectionFactory`
4. **Update Dependencies**: Ensure all JMS provider libraries support Jakarta Messaging
5. **Test Thoroughly**: Behavior should remain the same, but verify all integrations