/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.exceptions;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.tck.junit4.FunctionalTestCase;

import static org.junit.Assert.assertThat;

public class ExceptionStrategyFlowRefTestCase extends FunctionalTestCase
{

    public static final String MESSAGE = "some message";
    public static final int TIMEOUT = 5000;

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/exceptions/exception-strategy-flow-ref.xml";
    }

    @Test
    public void testExceptionInFlowCalledWithFlowRef() throws Exception
    {
        LocalMuleClient client = muleContext.getClient();
        client.send("vm://inExceptionBlock", MESSAGE, null, TIMEOUT);
        MuleMessage response = client.request("jms://dlq", TIMEOUT);
        assertThat(response, IsNull.<Object>notNullValue());
        assertThat(response.<String>getInboundProperty("mainEs"), Is.is("yes"));
        assertThat(response.<String>getInboundProperty("flowRefEs"), Is.is("yes"));
    }
}
