/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.routing.nested;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.config.i18n.MessageFactory;
import org.mule.tck.AbstractServiceAndFlowTestCase;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class BindingExceptionOnInterfaceMethodTestCase extends AbstractServiceAndFlowTestCase
{
    private static final String PREFIX = "Exception in service component: ";

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE,
                "org/mule/test/integration/routing/nested/binding-exception-on-interface-method-service.xml"},
            {ConfigVariant.FLOW,
                "org/mule/test/integration/routing/nested/binding-exception-on-interface-method-flow.xml"}});
    }

    public BindingExceptionOnInterfaceMethodTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testExceptionOnBinding() throws Exception
    {
        MuleMessage reply = muleContext.getClient().send("vm://invoker.in", TEST_MESSAGE, null);
        assertNotNull(reply);
        String payload = reply.getPayloadAsString();
        assertTrue(payload.contains("MuleRuntimeException"));
        assertTrue(payload.contains(PREFIX));
    }

    public static class Component
    {
        private BindigInterface binding;

        public String invoke(String payload)
        {
            try
            {
                binding.process(payload, Integer.valueOf(0xC0DE));
            }
            catch (MuleRuntimeException muleException)
            {
                return PREFIX + muleException.toString();
            }

            return "ERROR, should not have come here";
        }

        public BindigInterface getBinding()
        {
            return binding;
        }

        public void setBinding(BindigInterface binding)
        {
            this.binding = binding;
        }
    }

    public static class ExceptionThrowingService
    {
        public String process(String s, Integer v)
        {
            throw new MuleRuntimeException(MessageFactory.createStaticMessage("Boom"));
        }
    }

    public interface BindigInterface
    {
        String process(String s, Integer v) throws MuleRuntimeException;
    }

}
