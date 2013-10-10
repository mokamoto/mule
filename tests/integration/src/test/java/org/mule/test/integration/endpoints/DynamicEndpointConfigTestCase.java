/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.endpoints;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.DataType;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DynamicEndpointConfigTestCase extends AbstractServiceAndFlowTestCase
{
    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/test/integration/endpoints/dynamic-endpoint-config-service.xml"},
            {ConfigVariant.FLOW, "org/mule/test/integration/endpoints/dynamic-endpoint-config-flow.xml"}
        });
    }

    public DynamicEndpointConfigTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testName() throws Exception
    {
        MuleMessage msg = new DefaultMuleMessage("Data", muleContext);
        msg.setOutboundProperty("testProp", "testPath");
        MuleMessage response = muleContext.getClient().send("vm://in1", msg);
        assertNotNull(response);
        assertNull(response.getExceptionPayload());
        assertEquals("Data Received", response.getPayload(DataType.STRING_DATA_TYPE));

        response = muleContext.getClient().send("vm://in2", msg);
        assertNotNull(response);
        assertNull(response.getExceptionPayload());
        assertEquals("Data Received", response.getPayload(DataType.STRING_DATA_TYPE));

        response = muleContext.getClient().send("vm://in3", msg);
        assertNotNull(response);
        assertNull(response.getExceptionPayload());
        String payload = response.getPayload(DataType.STRING_DATA_TYPE);
        assertEquals("Data Also Received", payload);
    }
}
