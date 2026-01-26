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
package org.ops4j.pax.jms.test.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.jms.config.ConfigLoader;
import org.ops4j.pax.jms.service.ConnectionFactoryFactory;
import org.ops4j.pax.jms.test.AbstractArtemisTest;
import org.ops4j.pax.jms.test.AbstractJmsTest;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Uses the pax-jms-config module to create an Artemis ConnectionFactory from a configuration and validates the
 * ConnectionFactory is present as a service
 */
@RunWith(PaxExam.class)
public class ArtemisConfigTest extends AbstractArtemisTest {

    private static final String JNDI_NAME = "osgi.jndi.service.name";

    @Configuration
    public Option[] config() {
        return getCombine();
    }

    @Test
    public void testConnectionFactoryFromConfig() throws JMSException, IOException, InvalidSyntaxException, InterruptedException {
        org.osgi.service.cm.Configuration config = createConfigForConnectionFactory();
        ServiceTracker<ConnectionFactory, ConnectionFactory> tracker = new ServiceTracker<>(context, ConnectionFactory.class, null);
        tracker.open();
        ConnectionFactory cf = tracker.waitForService(2000);
        assertConnectionFactoryWorks(cf);
        assertServicePropertiesPresent(tracker.getServiceReference());
        checkConnectionFactoryIsDeletedWhenConfigIsDeleted(config, tracker);
        tracker.close();
    }

    @Test
    public void testConnectionFactoryFromEncryptedConfig() throws JMSException, IOException, InvalidSyntaxException, InterruptedException {
        StandardPBEStringEncryptor textEncryptor = new StandardPBEStringEncryptor();
        textEncryptor.setPassword("changeit!!!");
        textEncryptor.setKeyObtentionIterations(42);
        textEncryptor.setIvGenerator(new RandomIvGenerator());
        textEncryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_128");
        String encryptedUrl = textEncryptor.encrypt(brokerUrl);

        Dictionary<String, Object> props = new Hashtable<>();
        props.put("alias", "jasypt");
        context.registerService(StringEncryptor.class, textEncryptor, props);

        org.osgi.service.cm.Configuration config = createEncryptedConfigForConnectionFactory("ENC(" + encryptedUrl + ")");
        ServiceTracker<ConnectionFactory, ConnectionFactory> tracker = new ServiceTracker<>(context, ConnectionFactory.class, null);
        tracker.open();
        ConnectionFactory cf = tracker.waitForService(2000);
        assertConnectionFactoryWorks(cf);
        checkConnectionFactoryIsDeletedWhenConfigIsDeleted(config, tracker);
        tracker.close();
    }

    @Test
    public void testTwoConnectionFactorysFromConfig() throws Exception {
        org.osgi.service.cm.Configuration config1 = createConfigForConnectionFactory("cf1");
        org.osgi.service.cm.Configuration config2 = createConfigForConnectionFactory("cf2");
        ServiceTracker<ConnectionFactory, ConnectionFactory> tracker1 = new ServiceTracker<>(context, context.createFilter("(&(objectClass=" + ConnectionFactory.class.getName() + ")(osgi.jndi.service.name=cf1))"), null);
        ServiceTracker<ConnectionFactory, ConnectionFactory> tracker2 = new ServiceTracker<>(context, context.createFilter("(&(objectClass=" + ConnectionFactory.class.getName() + ")(osgi.jndi.service.name=cf2))"), null);
        tracker1.open();
        tracker2.open();
        ConnectionFactory cf1 = tracker1.waitForService(2000);
        ConnectionFactory cf2 = tracker2.waitForService(2000);
        assertConnectionFactoryWorks(cf1);
        assertConnectionFactoryWorks(cf2);
        assertServicePropertiesPresent(tracker1.getServiceReference(), "cf1");
        assertServicePropertiesPresent(tracker2.getServiceReference(), "cf2");

        FrameworkUtil.getBundle(ConfigLoader.class).stop();

        tracker1.close();
        tracker2.close();
    }

    private org.osgi.service.cm.Configuration createConfigForConnectionFactory() throws IOException {
        return createConfigForConnectionFactory("artemisTest");
    }

    private org.osgi.service.cm.Configuration createConfigForConnectionFactory(String jndiName) throws IOException {
        org.osgi.service.cm.Configuration config = configAdmin.createFactoryConfiguration("org.ops4j.connectionfactory", null);
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(ConnectionFactoryFactory.JMS_CONNECTIONFACTORY_TYPE, "artemis");
        props.put(ConnectionFactoryFactory.JMS_URL, brokerUrl);
        props.put(JNDI_NAME, jndiName); // jndi name for aries jndi
        config.update(props);
        return config;
    }

    private org.osgi.service.cm.Configuration createEncryptedConfigForConnectionFactory(String url) throws IOException {
        org.osgi.service.cm.Configuration config = configAdmin.createFactoryConfiguration("org.ops4j.connectionfactory", null);
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(ConnectionFactoryFactory.JMS_CONNECTIONFACTORY_TYPE, "artemis");
        props.put(ConnectionFactoryFactory.JMS_URL, url);
        props.put("decryptor", "jasypt");
        props.put(JNDI_NAME, "artemisEncryptedTest");
        config.update(props);
        return config;
    }

    private void assertConnectionFactoryWorks(ConnectionFactory cf) throws JMSException {
        assertNotNull("No ConnectionFactory service found", cf);
        cf.createConnection().close();
    }

    private void assertServicePropertiesPresent(ServiceReference<?> ref) {
        assertServicePropertiesPresent(ref, "artemisTest");
    }

    private void assertServicePropertiesPresent(ServiceReference<?> ref, String jndiName) {
        Assert.assertEquals(brokerUrl, ref.getProperty(ConnectionFactoryFactory.JMS_URL));
        Assert.assertEquals(jndiName, ref.getProperty(JNDI_NAME));
    }

    private void checkConnectionFactoryIsDeletedWhenConfigIsDeleted(
            org.osgi.service.cm.Configuration config, ServiceTracker<ConnectionFactory, ConnectionFactory> tracker)
            throws IOException, InterruptedException {
        config.delete();
        Thread.sleep(200);
        assertNull(tracker.getService());
    }

}
