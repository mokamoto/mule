/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.routing.filters.xml;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.module.xml.filters.IsXmlFilter;
import org.mule.module.xml.util.XMLTestUtils;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.util.IOUtils;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class IsXmlFilterTestCase extends AbstractMuleTestCase
{
    private IsXmlFilter filter;
    private MuleContext muleContext;

    @Before
    public void setUp()
    {
        filter = new IsXmlFilter();
        muleContext = mock(MuleContext.class);
    }

    @Test
    public void testFilterFalse() throws Exception
    {
        assertFalse(filter.accept(new DefaultMuleMessage("This is definitely not XML.", muleContext)));
    }

    @Test
    public void testFilterFalse2() throws Exception
    {
        assertFalse(filter.accept(new DefaultMuleMessage(
            "<line>This is almost XML</line><line>This is almost XML</line>", muleContext)));
    }

    @Test
    public void testFilterTrue() throws Exception
    {
        assertTrue(filter.accept(new DefaultMuleMessage("<msg attrib=\"att1\">This is some nice XML!</msg>", muleContext)));
    }

    @Test
    public void testFilterBytes() throws Exception
    {
        byte[] bytes = "<msg attrib=\"att1\">This is some nice XML!</msg>".getBytes();
        assertTrue(filter.accept(new DefaultMuleMessage(bytes, muleContext)));
    }

    @Test
    public void testFilterNull() throws Exception
    {
        assertFalse(filter.accept(new DefaultMuleMessage(null, muleContext)));
    }

    @Test
    public void testFilterLargeXml() throws Exception
    {
        InputStream is = IOUtils.getResourceAsStream("cdcatalog.xml", getClass());
        assertNotNull("Test resource not found.", is);
        final String xml = IOUtils.toString(is);
        assertTrue(filter.accept(new DefaultMuleMessage(xml, muleContext)));
    }

    @Test
    public void testFilterLargeXmlCompliantHtml() throws Exception
    {
        InputStream is = IOUtils.getResourceAsStream("cdcatalog.html", getClass());
        assertNotNull("Test resource not found.", is);
        final String html = IOUtils.toString(is);
        assertTrue(filter.accept(new DefaultMuleMessage(html, muleContext)));
    }

    @Test
    public void testFilterXmlMessageVariants() throws Exception
    {
        List<?> list = XMLTestUtils.getXmlMessageVariants("cdcatalog.xml");
        for (Object message : list)
        {
            assertTrue(filter.accept(new DefaultMuleMessage(message, muleContext)));
        }
    }
}
