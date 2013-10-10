/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.config.spring;

import org.mule.api.MuleContext;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.testmodels.fruit.Orange;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApplicationContextsTestCase extends AbstractMuleTestCase
{

    @Test
    public void testSanity() throws Exception
    {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("application-context.xml");
        
        Object orange = appContext.getBean("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        
        try
        {
            appContext.getBean("plum");
            fail("Bean should not have been found");
        }
        catch (NoSuchBeanDefinitionException e)
        {
            // expected
        }
    }

    /** 
     * Test that an existing appContext can be added to Mule's internal Registries 
     */
    @Test
    public void testSpringConfigurationBuilder() throws Exception
    {
        MuleContext context = new DefaultMuleContextFactory().createMuleContext();
        
        ApplicationContext appContext = new ClassPathXmlApplicationContext("application-context.xml");
        ConfigurationBuilder builder = new SpringConfigurationBuilder(appContext);
        builder.configure(context); 

        context.start();
        
        Object orange = context.getRegistry().lookupObject("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        assertEquals("Pirulo", ((Orange) orange).getBrand());
    }

    /** 
     * Test that the same bean from the 2nd appContext will have precedence over the 1st appContext 
     */
    @Test
    public void testSpringConfigurationBuilderPrecedence() throws Exception
    {
        MuleContext context = new DefaultMuleContextFactory().createMuleContext();
        
        ApplicationContext appContext = new ClassPathXmlApplicationContext("application-context.xml");
        ConfigurationBuilder builder = new SpringConfigurationBuilder(appContext);
        builder.configure(context); 

        appContext = new ClassPathXmlApplicationContext("application-context-2.xml");
        builder = new SpringConfigurationBuilder(appContext);
        builder.configure(context); 

        context.start();
        
        Object orange = context.getRegistry().lookupObject("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        assertEquals("Tropicana", ((Orange) orange).getBrand());
    }

    @Test
    public void testSpringConfigurationBuilderBackwardsPrecedence() throws Exception
    {
        MuleContext context = new DefaultMuleContextFactory().createMuleContext();
        
        ApplicationContext appContext = new ClassPathXmlApplicationContext("application-context-2.xml");
        ConfigurationBuilder builder = new SpringConfigurationBuilder(appContext);
        builder.configure(context); 

        appContext = new ClassPathXmlApplicationContext("application-context.xml");
        builder = new SpringConfigurationBuilder(appContext);
        builder.configure(context); 

        context.start();
        
        Object orange = context.getRegistry().lookupObject("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        assertEquals("Pirulo", ((Orange) orange).getBrand());
    }

    /** 
     * Test that the same bean from the TransientRegistry will have precedence over the 1st appContext 
     */
    @Test
    public void testTransientRegistryPrecedence() throws Exception
    {
        MuleContext context = new DefaultMuleContextFactory().createMuleContext();
        
        context.getRegistry().registerObject("orange", new Orange(12, 5.5, "Tutti Frutti"));
        
        ApplicationContext appContext = new ClassPathXmlApplicationContext("application-context.xml");
        ConfigurationBuilder builder = new SpringConfigurationBuilder(appContext);
        builder.configure(context); 

        context.start();
        
        Object orange = context.getRegistry().lookupObject("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        assertEquals("Tutti Frutti", ((Orange) orange).getBrand());
    }

    /** 
     * Test that an existing appContext can be used as a parent AppContext for Mule 
     */
    @Test
    public void testParentContext() throws Exception
    {
        MuleContext context = new DefaultMuleContextFactory().createMuleContext();

        ApplicationContext appContext = new ClassPathXmlApplicationContext("application-context.xml");

        SpringXmlConfigurationBuilder builder = new SpringXmlConfigurationBuilder("mule-config.xml");
        builder.setParentContext(appContext);
        builder.configure(context);

        context.start();

        Object orange = context.getRegistry().lookupObject("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        assertEquals("Pirulo", ((Orange) orange).getBrand());
    }

    /**
     * Test the most common approach: Create the Spring config + Mule config in a single AppContext.
     */
    @Test
    public void testAppContextTogetherWithMuleConfig() throws Exception
    {
        MuleContext context = new DefaultMuleContextFactory().createMuleContext();

        SpringXmlConfigurationBuilder builder = new SpringXmlConfigurationBuilder("application-context.xml, mule-config.xml");
        builder.configure(context);

        context.start();

        Object orange = context.getRegistry().lookupObject("orange");
        assertNotNull(orange);
        assertTrue(orange instanceof Orange);
        assertEquals("Pirulo", ((Orange) orange).getBrand());
    }
}
