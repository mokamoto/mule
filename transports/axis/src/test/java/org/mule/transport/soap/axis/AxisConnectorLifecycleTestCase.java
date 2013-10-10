/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.transport.soap.axis;

import org.mule.api.MuleException;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AxisConnectorLifecycleTestCase extends FunctionalTestCase
{

    private static String SERVICE_NAME = "mycomponent";
    private static String PROTOCOL_SERVICE_NAME = AxisConnector.AXIS_SERVICE_PROPERTY + "connector.axis.0";

    @Rule
    public DynamicPort dynamicPort1 = new DynamicPort("port1");

    @Rule
    public DynamicPort dynamicPort2 = new DynamicPort("port2");

    @Rule
    public DynamicPort dynamicPort3 = new DynamicPort("port3");

    @Override
    protected String getConfigResources()
    {
        return "axis-http-mule-config.xml";
    }

    /**
     * MULE-4570, MULE-4573
     * 
     * @throws MuleException
     */
    @Test
    public void testStopService() throws MuleException
    {
        muleContext.getRegistry().lookupService(SERVICE_NAME).stop();
        assertFalse(muleContext.getRegistry().lookupService(SERVICE_NAME).isStarted());
        assertFalse(muleContext.getRegistry().lookupService(PROTOCOL_SERVICE_NAME).isStarted());
    }

    /**
     * MULE-4570, MULE-4573
     * 
     * @throws MuleException
     */
    @Test
    public void testDisposeService() throws MuleException
    {
        muleContext.getRegistry().lookupService(SERVICE_NAME).dispose();
        assertFalse(muleContext.getRegistry().lookupService(SERVICE_NAME).isStarted());
        assertNull(muleContext.getRegistry().lookupService(PROTOCOL_SERVICE_NAME));
    }

    /**
     * MULE-4569, MULE-4573
     * 
     * @throws MuleException
     */
    @Test
    public void testRestartService() throws MuleException
    {
        muleContext.getRegistry().lookupService(SERVICE_NAME).stop();
        assertFalse(muleContext.getRegistry().lookupService(SERVICE_NAME).isStarted());
        assertFalse(muleContext.getRegistry().lookupService(PROTOCOL_SERVICE_NAME).isStarted());
        muleContext.getRegistry().lookupService(SERVICE_NAME).start();
        assertTrue(muleContext.getRegistry().lookupService(SERVICE_NAME).isStarted());
        assertTrue(muleContext.getRegistry().lookupService(PROTOCOL_SERVICE_NAME).isStarted());
    }

}
