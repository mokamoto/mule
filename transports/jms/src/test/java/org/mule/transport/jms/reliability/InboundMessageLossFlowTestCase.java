/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.transport.jms.reliability;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;


/**
 * Verify that no inbound messages are lost when exceptions occur.
 * The message must either make it all the way to the SEDA queue (in the case of
 * an asynchronous inbound endpoint), or be restored/rolled back at the source.
 *
 * In the case of JMS, this will cause the failed message to be redelivered if
 * JMSRedelivery is configured.
 */
public class InboundMessageLossFlowTestCase extends InboundMessageLossTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "reliability/activemq-config.xml, reliability/inbound-message-loss-flow.xml";
    }

    public void testTransformerException() throws Exception
    {
        putMessageOnQueue("transformerException");

        // Exception occurs after the SEDA queue for an asynchronous request, so from the client's
        // perspective, the message has been delivered successfully.
        // Note that this behavior is different from services because the exception occurs before
        // the SEDA queue for services.
        assertFalse("Message should not have been redelivered",
            messageRedelivered.await(latchTimeout, TimeUnit.MILLISECONDS));
    }

    public void testRouterException() throws Exception
    {
        putMessageOnQueue("routerException");

        // Exception occurs after the SEDA queue for an asynchronous request, so from the client's
        // perspective, the message has been delivered successfully.
        // Note that this behavior is different from services because the exception occurs before
        // the SEDA queue for services.
        assertFalse("Message should not have been redelivered",
            messageRedelivered.await(latchTimeout, TimeUnit.MILLISECONDS));
    }
}
