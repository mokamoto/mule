/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.jersey.exception;

public class HelloWorldException extends Exception
{
    private static final long serialVersionUID = -2552082535661134463L;

    public HelloWorldException(String message)
    {
        super(message);
    }
}
