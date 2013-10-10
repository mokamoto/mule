/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.routing;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.routing.RoutingException;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.simple.ByteArrayToHexString;
import org.mule.transformer.simple.SerializableToByteArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <code>IdempotentSecureHashMessageFilter</code> ensures that only unique messages are
 * received by a service. It does this by calculating the SHA-256 hash of the message
 * itself. This provides a value with an infinitesimally small chance of a collision. This
 * can be used to filter message duplicates. Please keep in mind that the hash is
 * calculated over the entire byte array representing the message, so any leading or
 * trailing spaces or extraneous bytes (like padding) can produce different hash values
 * for the same semantic message content. Care should be taken to ensure that messages do
 * not contain extraneous bytes. This class is useful when the message does not support
 * unique identifiers.
 */

public class IdempotentSecureHashMessageFilter extends IdempotentMessageFilter
{
    private String messageDigestAlgorithm = "SHA-256";

    private final SerializableToByteArray objectToByteArray = new SerializableToByteArray();
    private final ByteArrayToHexString byteArrayToHexString = new ByteArrayToHexString();

    @Override
    protected String getIdForEvent(MuleEvent event) throws MessagingException
    {
        try
        {
            Object payload = event.getMessage().getPayload();
            byte[] bytes = (byte[]) objectToByteArray.transform(payload);
            MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithm);
            byte[] digestedBytes = md.digest(bytes);
            return (String)byteArrayToHexString.transform(digestedBytes);
        }
        catch (NoSuchAlgorithmException nsa)
        {
            throw new RoutingException(event,this, nsa);
        }
        catch (TransformerException te)
        {
            throw new RoutingException(event, this, te);
        }
    }

    public String getMessageDigestAlgorithm()
    {
        return messageDigestAlgorithm;
    }

    public void setMessageDigestAlgorithm(String messageDigestAlgorithm)
    {
        this.messageDigestAlgorithm = messageDigestAlgorithm;
    }
}
