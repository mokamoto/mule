/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.config.spring.parsers;

import org.mule.config.spring.parsers.assembly.configuration.PropertyConfiguration;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Extra functionality exposed by child parsers
 */
public interface MuleChildDefinitionParser extends MuleDefinitionParser
{

    public void forceParent(BeanDefinition parent);

    public PropertyConfiguration getTargetPropertyConfiguration();

}
