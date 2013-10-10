/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.config.spring.parsers;

import org.mule.config.spring.parsers.beans.OrphanBean;

import org.junit.Test;

/**
 * This constructs a <em>temporary</em> bean whose contents are injected into a parent map by
 * {@link org.mule.config.spring.parsers.assembly.DefaultBeanAssembler}.  Since this occurs
 * <em>before</em> child elements are processed this will <em>cannot</em> handle nested elements.
 */
public class MapEntryCombinerTestCase extends AbstractNamespaceTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/config/spring/parsers/map-entry-combiner-test.xml";
    }

    @Test
    public void testProperties()
    {
        OrphanBean bean = (OrphanBean) assertBeanExists("checkProps", OrphanBean.class);
        logger.info("Map size: " + bean.getMap().size());
        assertMapEntryExists(bean.getMap(), "0", 0);
    }

    @Test
    public void testCombinedMap()
    {
        OrphanBean bean = (OrphanBean) assertBeanExists("orphan", OrphanBean.class);
        logger.info("Map size: " + bean.getMap().size());
        for (int i = 0; i < 6; ++i)
        {
            assertMapEntryExists(bean.getMap(), Integer.toString(i+1), i+1);
        }
    }

    @Test
    public void testReversedOrder()
    {
        OrphanBean bean = (OrphanBean) assertBeanExists("orphan2", OrphanBean.class);
        logger.info("Map size: " + bean.getMap().size());
        for (int i = 0; i < 2; ++i)
        {
            assertMapEntryExists(bean.getMap(), Integer.toString(i+1), i+1);
        }

    }

}
