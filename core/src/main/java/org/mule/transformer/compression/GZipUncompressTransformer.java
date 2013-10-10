/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.transformer.compression;

import org.mule.api.transformer.DataType;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.SerializationUtils;
import org.mule.util.compression.GZipCompression;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.SerializationException;

/**
 * <code>GZipCompressTransformer</code> will uncompress a byte[] or InputStream
 */
public class GZipUncompressTransformer extends AbstractCompressionTransformer
{
    public GZipUncompressTransformer()
    {
        super();
        this.setStrategy(new GZipCompression());
        this.registerSourceType(DataTypeFactory.BYTE_ARRAY);
        this.registerSourceType(DataTypeFactory.INPUT_STREAM);
        // No type checking for the return type by default. It could either be a byte array, an input stream or an object.
        this.setReturnDataType(DataTypeFactory.OBJECT);
    }

    @Override
    public Object doTransform(Object src, String outputEncoding) throws TransformerException
    {
        try
        {
            if (src instanceof InputStream)
            {
                return getStrategy().uncompressInputStream((InputStream) src);
            }
            else
            {
                byte[] buffer = getStrategy().uncompressByteArray((byte[]) src);
                DataType<?> returnDataType = getReturnDataType();

                // If a return type has been specified, then deserialize the uncompressed byte array.
                if (!DataTypeFactory.OBJECT.equals(returnDataType) && !DataTypeFactory.BYTE_ARRAY.equals(returnDataType))
                {
                    return SerializationUtils.deserialize(buffer, muleContext);
                }
                else
                {
                    // First try to deserialize the byte array. If it can be deserialized, then it was originally serialized.
                    try
                    {
                        return SerializationUtils.deserialize(buffer, muleContext);
                    }
                    catch (SerializationException e)
                    {
                        // If it fails, ignore it. We assume it was not serialized in the first place and return the buffer as it is.
                        return buffer;
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new TransformerException(
                    MessageFactory.createStaticMessage("Failed to uncompress message."), this, e);
        }
    }
}
