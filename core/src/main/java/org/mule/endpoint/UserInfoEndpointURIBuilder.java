/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.endpoint;

import org.mule.api.endpoint.MalformedEndpointException;

import java.net.URI;
import java.util.Properties;

/**
 * <code>UserInfoEndpointBuilder</code> builds an endpoint with the userinfo and
 * host details. This endpoint builder is used where endpoints as of the form :
 * xxx://ross:secret@host:1000
 */
public class UserInfoEndpointURIBuilder extends AbstractEndpointURIBuilder
{
    //TODO THis endpoint builder is redundant I think. We should be able to use the URL endpoint builder.
    //It depends on where deriving classes can work with the URL endpoint builder, but there are a lot of similarities
    protected void setEndpoint(URI uri, Properties props) throws MalformedEndpointException
    {
        // Added by Lajos 2006-12-14 per Ross
        if (uri.getHost() == null)
        {
            if (props.getProperty("address") == null)
            {
                throw new MalformedEndpointException(uri.toString());
            }
            else
            {
                return;
            }
        }

        // Check and handle '@' symbols in the user info
        address = uri.getHost();
        int a = address.indexOf(".");
        int b = (a == -1 ? -1 : address.indexOf(".", a + 1));
        if (b > -1)
        {
            address = address.substring(a + 1);
        }

        if (uri.getPort() != -1)
        {
            // set the endpointUri to be a proper url if host and port are set
            this.address += ":" + uri.getPort();
        }

        if (userInfo != null)
        {
            int x = userInfo.indexOf(":");
            if (x > -1)
            {
                String user = userInfo.substring(0, x);
                if (user.indexOf("@") > -1)
                {
                    address = user;
                }
                else
                {
                    address = user + "@" + address;
                }
            }
            else
            {
                if (userInfo.indexOf("@") > -1)
                {
                    address = userInfo;
                }
                else
                {
                    address = userInfo + "@" + address;
                }
            }
        }
    }
}
