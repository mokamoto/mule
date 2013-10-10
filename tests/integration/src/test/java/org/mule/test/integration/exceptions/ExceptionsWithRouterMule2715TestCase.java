/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.exceptions;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerMessagingException;
import org.mule.message.ExceptionMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExceptionsWithRouterMule2715TestCase extends AbstractServiceAndFlowTestCase
{
    public static final String MESSAGE = "message";
    public static final long TIMEOUT = 5000L;

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/test/integration/exceptions/exceptions-with-router-mule-2715-service.xml"},
            {ConfigVariant.FLOW, "org/mule/test/integration/exceptions/exceptions-with-router-mule-2715-flow.xml"}
        });
    }

    public ExceptionsWithRouterMule2715TestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testWithRouter() throws Exception
    {
        doTest("with-router-in");
    }

    @Test
    public void testWithoutRouter() throws Exception
    {
        doTest("without-router-in");
    }

    protected void doTest(String path) throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        client.dispatch("vm://" + path, MESSAGE, null);
        MuleMessage response = client.request("vm://error", TIMEOUT);
        assertNotNull(response);
        assertTrue(response.getPayload() instanceof ExceptionMessage);
        assertTrue(((ExceptionMessage) response.getPayload()).getException() instanceof TransformerMessagingException);
    }
}
