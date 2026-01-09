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
package org.ops4j.pax.jms.artemis;

import java.util.HashMap;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQQueueConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.ops4j.pax.jms.service.ConnectionFactoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConnectTest {

    public static final Logger LOG = LoggerFactory.getLogger(ConnectTest.class);

    @ClassRule
    public static GenericContainer<?> artemis = new GenericContainer<>(DockerImageName.parse("quay.io/artemiscloud/activemq-artemis-broker:latest"))
            .withExposedPorts(61616)
            .withEnv("AMQ_USER", "admin")
            .withEnv("AMQ_PASSWORD", "admin");

    private static String brokerUrl;

    @BeforeClass
    public static void setUp() {
        brokerUrl = "tcp://" + artemis.getHost() + ":" + artemis.getMappedPort(61616);
        LOG.info("Artemis broker URL: {}", brokerUrl);
    }

    @Test
    public void jmsConnect() throws Exception {
        ConnectionFactory cf = new ActiveMQQueueConnectionFactory(brokerUrl);
        ConnectionMetaData md;
        try (Connection con = cf.createConnection("admin", "admin")) {
            con.start();
            md = con.getMetaData();
            LOG.info("{}/{}", md.getJMSProviderName(), md.getProviderVersion());

            ActiveMQQueue dest = new ActiveMQQueue("q1");

            try (Session session = con.createSession()) {
                try (MessageProducer producer = session.createProducer(dest)) {
                    TextMessage tm = session.createTextMessage("Hello!");
                    producer.send(tm);
                }
            }
        }
        // JMS 2.0 API
        try (JMSContext ctx = cf.createContext("admin", "admin")) {
            ctx.start();
            ActiveMQQueue dest = new ActiveMQQueue("q1");

            try (JMSConsumer consumer = ctx.createConsumer(dest)) {
                TextMessage tm = (TextMessage) consumer.receive();
                assertThat(tm.getText(), equalTo("Hello!"));
            }
        }
    }

    @Test
    public void paxJmsConnect() throws Exception {
        ConnectionFactoryFactory ff = new ArtemisConnectionFactoryFactory();
        HashMap<String, Object> props = new HashMap<>();
        props.put(ConnectionFactoryFactory.JMS_URL, brokerUrl);
        props.put(ConnectionFactoryFactory.JMS_USER, "admin");
        props.put(ConnectionFactoryFactory.JMS_PASSWORD, "admin");
        ConnectionMetaData md;
        try (Connection con = ff.createConnectionFactory(props).createConnection()) {
            con.start();
            md = con.getMetaData();
            LOG.info("{}/{}", md.getJMSProviderName(), md.getProviderVersion());

            ActiveMQQueue dest = new ActiveMQQueue("q1");

            try (Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                try (MessageProducer producer = session.createProducer(dest)) {
                    TextMessage tm = session.createTextMessage("Hello!");
                    producer.send(tm);
                }
            }

            try (Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                try (MessageConsumer consumer = session.createConsumer(dest)) {
                    TextMessage tm = (TextMessage) consumer.receive();
                    assertThat(tm.getText(), equalTo("Hello!"));
                }
            }
        }
    }

}
