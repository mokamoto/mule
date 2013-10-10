/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.cxf.wssec;

import static org.junit.Assert.assertTrue;

import org.mule.tck.AbstractServiceAndFlowTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.util.concurrent.Latch;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class UsernameTokenTestCase extends AbstractServiceAndFlowTestCase
{
    private Latch greetLatch;

    @Rule
    public DynamicPort dynamicPort = new DynamicPort("port1");
    
    public UsernameTokenTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/module/cxf/wssec/cxf-secure-service-service.xml, org/mule/module/cxf/wssec/username-token-conf.xml"},
            {ConfigVariant.FLOW, "org/mule/module/cxf/wssec/cxf-secure-service-flow.xml, org/mule/module/cxf/wssec/username-token-conf.xml"}
        });
    }      
        
    @Override
    protected void doSetUp() throws Exception
    {
        ClientPasswordCallback.setPassword("secret");        
        super.doSetUp();
        
        greetLatch = getGreeter().getLatch();
    }

    @Test
    public void testUsernameToken() throws Exception
    {
        assertTrue(greetLatch.await(60, TimeUnit.SECONDS));
    }

    protected GreeterWithLatch getGreeter() throws Exception
    {
        Object instance = getComponent("greeterService");
        return (GreeterWithLatch) instance;
    }

}


