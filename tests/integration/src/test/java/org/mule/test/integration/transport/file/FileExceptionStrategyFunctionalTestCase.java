/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.test.integration.transport.file;

import org.mule.tck.AbstractServiceAndFlowTestCase;
import org.mule.util.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.fail;

public class FileExceptionStrategyFunctionalTestCase extends AbstractServiceAndFlowTestCase
{
    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE,
                "org/mule/test/integration/providers/file/file-exception-strategy-service.xml"},
            {ConfigVariant.FLOW, "org/mule/test/integration/providers/file/file-exception-strategy-flow.xml"}});
    }

    public FileExceptionStrategyFunctionalTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testExceptionInTransformer() throws Exception
    {
        File f = FileUtils.newFile("./.mule/in/test.txt");
        f.createNewFile();

        // try a couple of times with backoff strategy, then fail
        File errorFile = FileUtils.newFile("./.mule/errors/test-0.out");
        boolean testSucceded = false;
        int timesTried = 0;
        while (timesTried <= 3)
        {
            Thread.sleep(500 * ++timesTried);
            if (errorFile.exists())
            {
                testSucceded = true;
                break;
            }
        }

        if (!testSucceded)
        {
            fail("Exception strategy hasn't moved the file to the error folder.");
        }
    }
}
