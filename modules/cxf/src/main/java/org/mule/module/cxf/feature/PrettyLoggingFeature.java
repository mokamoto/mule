/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.cxf.feature;

import org.mule.api.MuleException;
import org.mule.module.xml.transformer.XmlPrettyPrinter;
import org.mule.util.StringUtils;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

/**
 * Same as the standard CXF LoggingFeature, but with pretty-printed XML 
 * for the message payload.
 */
public class PrettyLoggingFeature extends LoggingFeature
{
    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus)
    {
        provider.getInInterceptors().add(new PrettyLoggingInInterceptor(getLimit()));
        provider.getOutInterceptors().add(new PrettyLoggingOutInterceptor(getLimit()));
    }
    
    /**
     * Takes the log output from the standard CXF LoggingInterceptor, 
     * disassembles it, pretty-prints the XML payload, then puts it all back 
     * together again.
     */
    protected static String formatXmlPayload(String originalLogString)
    {
        String[] lines = originalLogString.split("\n");
        
        // Figure out which line has the payload on it
        int payloadLine = -1;
        for (int i=0; i<lines.length; ++i)
        {
            if (lines[i].startsWith("Payload: "))
            {
                payloadLine = i;
                break;
            }
        }
        if (payloadLine == -1)
        {
            System.err.println("Could not find a line which begins with 'Payload: '");
            return originalLogString;
        }
        
        // Extract the XML payload and format it
        String payload = lines[payloadLine];
        String xml = StringUtils.substringAfter(payload, "Payload: ");
        XmlPrettyPrinter pp = new XmlPrettyPrinter();
        try
        {
            xml = (String) pp.transform(xml);
        }
        catch (MuleException e)
        {
            System.err.println(e.getMessage());
        }
        
        // Replace the payload line with the formatted XML and put it all back together again
        lines[payloadLine] = "Payload: \n" + xml;
        return StringUtils.join(lines, "\n");
    }
}
