/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.components;

import org.mule.api.registry.Registry;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.services.UniqueComponent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ObjectFactoryTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/components/object-factory-functional-test.xml";
    }

    @Test
    public void testDefaultScope() throws Exception
    {
        Registry registry = muleContext.getRegistry();
        
        Object bean1 = registry.lookupObject("default");
        assertNotNull(bean1);
        String id1 = ((UniqueComponent) bean1).getId();
        
        Object bean2 = registry.lookupObject("default");
        assertNotNull(bean2);
        String id2 = ((UniqueComponent) bean2).getId();
        
        assertEquals(id1, id2);
    }

    @Test
    public void testSingletonScope() throws Exception
    {
        Registry registry = muleContext.getRegistry();
        
        Object bean1 = registry.lookupObject("singleton");
        assertNotNull(bean1);
        String id1 = ((UniqueComponent) bean1).getId();
        
        Object bean2 = registry.lookupObject("singleton");
        assertNotNull(bean2);
        String id2 = ((UniqueComponent) bean2).getId();
        
        assertEquals(id1, id2);
    }

    @Test
    public void testPrototypeScope() throws Exception
    {
        Registry registry = muleContext.getRegistry();
        
        Object bean1 = registry.lookupObject("prototype");
        assertNotNull(bean1);
        String id1 = ((UniqueComponent) bean1).getId();
        
        Object bean2 = registry.lookupObject("prototype");
        assertNotNull(bean2);
        String id2 = ((UniqueComponent) bean2).getId();
        
        assertFalse("IDs " + id1 + " and " + id2 + " should be different", id1.equals(id2));
    }

}


