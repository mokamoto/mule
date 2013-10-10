/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.config.spring.parsers.assembly;

import org.mule.config.spring.parsers.assembly.configuration.PropertyConfiguration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class TwoStageMapBeanAssemblerFactory implements BeanAssemblerFactory
{

    private BeanAssemblerStore store;

    public TwoStageMapBeanAssemblerFactory(BeanAssemblerStore store)
    {
        this.store = store;
    }

    public BeanAssembler newBeanAssembler(
            PropertyConfiguration beanConfig, BeanDefinitionBuilder bean,
            PropertyConfiguration targetConfig, BeanDefinition target)
    {
        return new TwoStageMapBeanAssembler(store, beanConfig, bean, targetConfig, target);
    }

    public interface BeanAssemblerStore
    {

        public void saveBeanAssembler(BeanAssembler beanAssembler);

    }


}
