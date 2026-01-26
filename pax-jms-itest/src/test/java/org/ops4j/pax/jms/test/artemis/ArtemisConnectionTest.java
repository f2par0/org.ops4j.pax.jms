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
package org.ops4j.pax.jms.test.artemis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import jakarta.jms.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.jms.artemis.ArtemisConnectionFactoryFactory;
import org.ops4j.pax.jms.service.ConnectionFactoryFactory;
import org.ops4j.pax.jms.test.AbstractArtemisTest;

/**
 * Uses the pax-jms-config module to create an Artemis ConnectionFactory from a configuration and validates the
 * ConnectionFactory is present as a service
 */
@RunWith(PaxExam.class)
public class ArtemisConnectionTest extends AbstractArtemisTest {

    @Configuration
    public Option[] config() {
        return getCombine();
    }

    @Test
    public void testConnectionUsingJmsApi() throws Exception {
        ConnectionFactoryFactory ff = new ArtemisConnectionFactoryFactory();
        HashMap<String, Object> props = new HashMap<>();
        props.put(ConnectionFactoryFactory.JMS_URL, brokerUrl);
        ConnectionMetaData md;
        try (Connection con = ff.createConnectionFactory(props).createConnection()) {
            con.start();
            md = con.getMetaData();
            LOG.info("{}/{}", md.getJMSProviderName(), md.getProviderVersion());

            try (Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                Queue dest = session.createQueue("q1");
                try (MessageProducer producer = session.createProducer(dest)) {
                    TextMessage tm = session.createTextMessage("Hello!");
                    producer.send(tm);
                }
            }

            try (Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                Queue dest = session.createQueue("q1");
                try (MessageConsumer consumer = session.createConsumer(dest)) {
                    TextMessage tm = (TextMessage) consumer.receive();
                    assertThat(tm.getText(), equalTo("Hello!"));
                }
            }
        }
    }
}
