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
package org.ops4j.pax.jms.test;

import static org.ops4j.pax.exam.OptionUtils.combine;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ops4j.pax.exam.Option;
import org.osgi.service.cm.ConfigurationAdmin;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractArtemisTest extends AbstractJmsTest {
    protected static GenericContainer<?> artemis;
    protected static String brokerUrl;

    @Inject
    protected ConfigurationAdmin configAdmin;

    @BeforeClass
    public static void setUp() {
        artemis = new GenericContainer<>(DockerImageName.parse("quay.io/artemiscloud/activemq-artemis-broker:latest"))
                .withExposedPorts(61616)
                .withEnv("AMQ_USER", "admin")
                .withEnv("AMQ_PASSWORD", "admin");
        artemis.start();
        brokerUrl = "tcp://" + artemis.getHost() + ":" + artemis.getMappedPort(61616);
        LOG.info("Artemis broker URL: {}", brokerUrl);
    }

    @AfterClass
    public static void tearDown() {
        if (artemis != null) {
            artemis.stop();
        }
    }

    protected Option @NotNull [] getCombine() {
        return combine(baseConfiguration(),
                mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt"),
                mvnBundle("org.ops4j.pax.jms", "pax-jms-api"), mvnBundle("org.ops4j.pax.jms", "pax-jms-config"),
                mvnBundle("org.ops4j.pax.jms", "pax-jms-artemis"),
                // we have to install all bundles required by artemis-core-client and artemis-jms-client features
                mvnBundle("jakarta.jms", "jakarta.jms-api"),
                // Artemis 2.50.0+ uses org.apache.artemis group ID and jakarta-client-osgi
                mvnBundle("org.apache.artemis", "artemis-jakarta-client-osgi"),
                mvnBundle("org.apache.activemq", "activemq-artemis-native"),
                mvnBundle("io.netty", "netty-buffer"),
                mvnBundle("io.netty", "netty-codec"),
                mvnBundle("io.netty", "netty-codec-socks"),
                mvnBundle("io.netty", "netty-common"),
                mvnBundle("io.netty", "netty-resolver"),
                mvnBundle("io.netty", "netty-transport"),
                mvnBundle("io.netty", "netty-transport-native-unix-common"),
                mvnBundle("io.netty", "netty-transport-classes-kqueue"),
                mvnBundle("io.netty", "netty-codec-http"),
                mvnBundle("io.netty", "netty-handler"),
                mvnBundle("io.netty", "netty-handler-proxy"),
                mvnBundle("io.netty", "netty-transport-classes-epoll"),
                mvnBundle("commons-beanutils", "commons-beanutils"),
                mvnBundle("commons-collections", "commons-collections"),
                mvnBundle("org.jgroups", "jgroups"),
                mvnBundle("org.jctools", "jctools-core"),
                mvnBundle("org.apache.johnzon", "johnzon-core"),
                mvnBundle("com.google.guava", "guava"),
                mvnBundle("com.google.guava", "failureaccess"),
                mvnBundle("javax.json", "javax.json-api"),
                mvnBundle("javax.mail", "javax.mail-api"),
                mvnBundle("com.sun.activation", "javax.activation"),
                mvnBundle("com.sun.mail", "javax.mail"));
    }
}
