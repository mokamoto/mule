/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.security;

import org.mule.api.EncryptionStrategy;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.transport.SessionHandler;
import org.mule.module.client.MuleClient;
import org.mule.session.MuleSessionHandler;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests multi-user security against a security provider which only authenticates
 * a single user at a time (i.e., authentication of a new user overwrites the
 * previous authentication).
 *
 * see EE-979
 */
@Ignore
public class MultiuserSecurityTestCase extends AbstractServiceAndFlowTestCase
{
    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "multiuser-security-test-service.xml, singleuser-security-provider.xml"},
            {ConfigVariant.FLOW, "multiuser-security-test-flow.xml, singleuser-security-provider.xml"}});
    }

    public MultiuserSecurityTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testMultipleAuthentications() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        SessionHandler sessionHandler = new MuleSessionHandler();
        MuleMessage reply;
        Map<String, Object> props;

        EncryptionStrategy strategy = muleContext.getSecurityManager().getEncryptionStrategy("PBE");

        props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_USER_PROPERTY, MuleCredentials.createHeader("marie", "marie", "PBE", strategy));
        reply = client.send("vm://test", "Data1", props);
        assertNotNull(reply);
        assertEquals("user = marie, logins = 1, color = bright red", reply.getPayload());

        props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_USER_PROPERTY, MuleCredentials.createHeader("stan", "stan", "PBE", strategy));
        reply = client.send("vm://test", "Data2", props);
        assertNotNull(reply);
        assertEquals("user = stan, logins = 1, color = metallic blue", reply.getPayload());

        props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_USER_PROPERTY, MuleCredentials.createHeader("cindy", "cindy", "PBE", strategy));
        reply = client.send("vm://test", "Data3", props);
        assertEquals("user = cindy, logins = 1, color = dark violet", reply.getPayload());

        props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_USER_PROPERTY, MuleCredentials.createHeader("marie", "marie", "PBE", strategy));
        reply = client.send("vm://test", "Data4", props);
        assertNotNull(reply);
        assertEquals("user = marie, logins = 2, color = bright red", reply.getPayload());

        props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_USER_PROPERTY, MuleCredentials.createHeader("marie", "marie", "PBE", strategy));
        reply = client.send("vm://test", "Data4", props);
        assertNotNull(reply);
        assertEquals("user = marie, logins = 3, color = bright red", reply.getPayload());

        props = new HashMap<String, Object>();
        props.put(MuleProperties.MULE_USER_PROPERTY, MuleCredentials.createHeader("stan", "stan", "PBE", strategy));
        reply = client.send("vm://test", "Data2", props);
        assertNotNull(reply);
        assertEquals("user = stan, logins = 2, color = metallic blue", reply.getPayload());
    }
}
