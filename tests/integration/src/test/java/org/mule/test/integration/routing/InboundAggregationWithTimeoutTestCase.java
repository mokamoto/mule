/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.routing;

import org.mule.api.context.notification.RoutingNotificationListener;
import org.mule.context.notification.RoutingNotification;
import org.mule.module.client.MuleClient;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertTrue;

public class InboundAggregationWithTimeoutTestCase extends AbstractServiceAndFlowTestCase
{
    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/test/integration/routing/multi-inbound-aggregator-with-timeout-service.xml"},
            {ConfigVariant.FLOW, "org/mule/test/integration/routing/multi-inbound-aggregator-with-timeout-flow.xml"}
        });
    }

    public InboundAggregationWithTimeoutTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testAggregatorTimeout() throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(1);

        muleContext.registerListener(new RoutingNotificationListener<RoutingNotification>()
        {
            @Override
            public void onNotification(RoutingNotification notification)
            {
                if (notification.getAction() == RoutingNotification.CORRELATION_TIMEOUT)
                {
                    latch.countDown();
                }
            }
        });

        String message = "test";
        MuleClient client = new MuleClient(muleContext);
        client.dispatch("vm://distributor.queue", message, null);

        assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    }
}
