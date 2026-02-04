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
package org.ops4j.pax.jms.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jms.service.PooledConnectionFactoryFactory;
import org.ops4j.pax.jms.test.AbstractJmsTest;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

public class PoolNarayanaTest extends AbstractJmsTest {

    @Inject
    @Filter("(pool=narayana)(xa=false)")
    PooledConnectionFactoryFactory pool;

    @Configuration
    public Option[] config() {
        return combine(
                baseConfiguration(),
                CoreOptions.bootDelegationPackage("sun.*"),
                CoreOptions.bootDelegationPackage("javax.transaction.xa.*"),
                mvnBundle("org.ops4j.pax.jms", "pax-jms-api"),
                mvnBundle("org.ops4j.pax.jms", "pax-jms-pool-narayana"),
                jakartaBundles(),
                mvnBundle("org.messaginghub", "pooled-jms"),
                wrappedBundle(mvnBundle("org.jboss.narayana.jta", "narayana-jta")),
                wrappedBundle(mvnBundle("org.jboss.narayana.jts", "narayana-jts-integration")),
                mvnBundle("org.ops4j.pax.transx", "pax-transx-tm-narayana"),
                mvnBundle("org.ops4j.pax.transx", "pax-transx-tm-api"),
                mvnBundle("org.apache.commons", "commons-pool2")
        );
    }

    @Test
    public void testNarayanaConnectionFactoryFactoryServicePresent() {
    }

}
