/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.routing.replyto;

import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.module.client.MuleClient;
import org.mule.tck.junit4.FunctionalTestCase;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReplyToChainIntegration1TestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/routing/replyto/replyto-chain-integration-test-1.xml";
    }

    @Test
    public void testReplyToChain() throws Exception
    {
        String message = "test";

        MuleClient client = new MuleClient(muleContext);
        Map props = new HashMap();
        props.put(MuleProperties.MULE_REMOTE_SYNC_PROPERTY, "false");
        MuleMessage result = client.send("vm://pojo1", message, props);
        assertNotNull(result);
        assertEquals("Received: " + message, result.getPayloadAsString());
    }
    
    @Test
    public void testReplyToChainWithoutProps() throws Exception
    {
        String message = "test";

        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("vm://pojo1", message, null);
        assertNotNull(result);
        assertEquals("Received: " + message, result.getPayloadAsString());
    }

}
