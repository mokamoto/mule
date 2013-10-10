/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.atom.transformers;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractDiscoverableTransformer;
import org.mule.transformer.types.DataTypeFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;

/**
 * <code>ObjectToInputStream</code> converts serilaizable object to a input stream but
 * treats <code>java.lang.String</code> differently by converting to bytes using
 * the <code>String.getBytes()</code> method.
 */
public class ObjectToFeed extends AbstractDiscoverableTransformer
{
    public ObjectToFeed()
    {
        this.registerSourceType(DataTypeFactory.BYTE_ARRAY);
        this.registerSourceType(DataTypeFactory.INPUT_STREAM);
        this.registerSourceType(DataTypeFactory.STRING);
        setReturnDataType(DataTypeFactory.create(Feed.class));
    }

    @Override
    public Object doTransform(Object src, String outputEncoding) throws TransformerException
    {
        try
        {
            Parser parser = Abdera.getInstance().getParser();
            Document<Element> doc;
            if (src instanceof InputStream)
            {
                doc = parser.parse((InputStream) src, outputEncoding);
            }
            else if (src instanceof byte[])
            {
                doc = parser.parse(new ByteArrayInputStream((byte[]) src), outputEncoding);
            }
            else
            {
                doc = parser.parse(new StringReader((String) src));
            }
            
            //we only need to check for the registered source types
            return doc.getRoot();
        }
        catch (Exception e)
        {
            throw new TransformerException(this, e);
        }
    }
}
