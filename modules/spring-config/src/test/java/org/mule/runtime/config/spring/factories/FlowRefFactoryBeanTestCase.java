/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.factories;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.setMuleContextIfNeeded;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.config.MuleProperties;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.el.ExpressionLanguage;
import org.mule.runtime.core.api.lifecycle.Disposable;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.processor.MessageProcessorChain;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.runtime.core.processor.chain.SubFlowMessageProcessor;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.size.SmallTest;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockSettings;
import org.springframework.context.ApplicationContext;

@SmallTest
public class FlowRefFactoryBeanTestCase extends AbstractMuleContextTestCase {

  private static final MockSettings INITIALIZABLE_MESSAGE_PROCESSOR =
      withSettings().extraInterfaces(Processor.class, Initialisable.class, Disposable.class);
  private static final String STATIC_REFERENCED_FLOW = "staticReferencedFlow";
  private static final String DYNAMIC_REFERENCED_FLOW = "dynamicReferencedFlow";
  private static final String PARSED_DYNAMIC_REFERENCED_FLOW = "parsedDynamicReferencedFlow";
  private static final String DYNAMIC_NON_EXISTANT = "#['nonExistant']";
  private static final String NON_EXISTANT = "nonExistant";

  private Event result = testEvent();
  private FlowConstruct targetFlow = mock(FlowConstruct.class, INITIALIZABLE_MESSAGE_PROCESSOR);
  private Processor targetSubFlow = mock(SubFlowMessageProcessor.class, INITIALIZABLE_MESSAGE_PROCESSOR);
  private ApplicationContext applicationContext = mock(ApplicationContext.class);
  private ExpressionLanguage expressionLanguage = mock(ExpressionLanguage.class);

  public FlowRefFactoryBeanTestCase() throws MuleException {}

  @Before
  public void setup() throws MuleException {
    muleContext.getRegistry().registerObject(MuleProperties.OBJECT_EXPRESSION_LANGUAGE, expressionLanguage);
    when(expressionLanguage.isExpression(anyString())).thenReturn(true);
    when(((Processor) targetFlow).process(any(Event.class))).thenReturn(result);
    when(targetSubFlow.process(any(Event.class))).thenReturn(result);
  }

  @Test
  public void staticFlowRefFlow() throws Exception {
    // Flow is wrapped to prevent lifecycle propagation
    FlowRefFactoryBean flowRefFactoryBean = createStaticFlowRefFactoryBean((Processor) targetFlow);

    assertNotSame(targetFlow, getFlowRefProcessor(flowRefFactoryBean));
    assertNotSame(targetFlow, getFlowRefProcessor(flowRefFactoryBean));

    verifyProcess(flowRefFactoryBean, (Processor) targetFlow, 0);
  }

  @Test
  public void dynamicFlowRefFlow() throws Exception {
    // Inner MessageProcessor is used to resolve MP in runtime
    FlowRefFactoryBean flowRefFactoryBean = createDynamicFlowRefFactoryBean((Processor) targetFlow);

    assertNotSame(targetFlow, getFlowRefProcessor(flowRefFactoryBean));
    assertNotSame(targetFlow, getFlowRefProcessor(flowRefFactoryBean));

    verifyProcess(flowRefFactoryBean, (Processor) targetFlow, 0);
  }

  @Test
  public void staticFlowRefSubFlow() throws Exception {
    FlowRefFactoryBean flowRefFactoryBean = createStaticFlowRefFactoryBean(targetSubFlow);

    // Processor is wrapped by factory bean implementation
    assertThat(targetSubFlow, not(equalTo(getFlowRefProcessor(flowRefFactoryBean))));
    assertThat(targetSubFlow, not(equalTo(getFlowRefProcessor(flowRefFactoryBean))));

    verifyProcess(flowRefFactoryBean, targetSubFlow, 0);
  }

  @Test
  public void dynamicFlowRefSubFlow() throws Exception {
    FlowRefFactoryBean flowRefFactoryBean = createDynamicFlowRefFactoryBean(targetSubFlow);

    // Inner MessageProcessor is used to resolve MP in runtime
    assertNotSame(targetSubFlow, getFlowRefProcessor(flowRefFactoryBean));
    assertNotSame(targetSubFlow, getFlowRefProcessor(flowRefFactoryBean));

    verifyProcess(flowRefFactoryBean, targetSubFlow, 1);
  }

  @Test
  public void dynamicFlowRefSubFlowConstructAware() throws Exception {
    FlowConstruct flowConstruct = mock(FlowConstruct.class);
    Event event = testEvent();
    FlowConstructAware targetSubFlowConstructAware = mock(FlowConstructAware.class, INITIALIZABLE_MESSAGE_PROCESSOR);
    when(((Processor) targetSubFlowConstructAware).process(event)).thenReturn(result);

    FlowRefFactoryBean flowRefFactoryBean = createDynamicFlowRefFactoryBean((Processor) targetSubFlowConstructAware);
    final Processor flowRefProcessor = getFlowRefProcessor(flowRefFactoryBean);
    ((FlowConstructAware) flowRefProcessor).setFlowConstruct(flowConstruct);
    assertSame(result, flowRefProcessor.process(event));

    verify(targetSubFlowConstructAware).setFlowConstruct(flowConstruct);
  }

  @Test
  public void dynamicFlowRefSubContextAware() throws Exception {
    Event event = testEvent();
    MuleContextAware targetMuleContextAwareAware = mock(MuleContextAware.class, INITIALIZABLE_MESSAGE_PROCESSOR);
    when(((Processor) targetMuleContextAwareAware).process(event)).thenReturn(result);

    FlowRefFactoryBean flowRefFactoryBean = createDynamicFlowRefFactoryBean((Processor) targetMuleContextAwareAware);
    assertSame(result, getFlowRefProcessor(flowRefFactoryBean).process(event));

    verify(targetMuleContextAwareAware).setMuleContext(muleContext);
  }

  @Test
  public void dynamicFlowRefSubFlowMessageProcessorChain() throws Exception {
    FlowConstruct flowConstruct = mock(FlowConstruct.class);
    Event event = testEvent();

    Processor targetSubFlowConstructAware =
        (Processor) mock(FlowConstructAware.class, INITIALIZABLE_MESSAGE_PROCESSOR);
    when(targetSubFlowConstructAware.process(event)).thenReturn(result);
    Processor targetMuleContextAwareAware =
        (Processor) mock(MuleContextAware.class, INITIALIZABLE_MESSAGE_PROCESSOR);
    when(targetMuleContextAwareAware.process(event)).thenReturn(result);

    MessageProcessorChain targetSubFlowChain = mock(MessageProcessorChain.class, INITIALIZABLE_MESSAGE_PROCESSOR);
    when(targetSubFlowChain.getMessageProcessors())
        .thenReturn(Arrays.asList(targetSubFlowConstructAware, targetMuleContextAwareAware));

    FlowRefFactoryBean flowRefFactoryBean = createDynamicFlowRefFactoryBean(targetSubFlowChain);
    final Processor flowRefProcessor = getFlowRefProcessor(flowRefFactoryBean);
    ((FlowConstructAware) flowRefProcessor).setFlowConstruct(flowConstruct);
    flowRefProcessor.process(event);

    verify((FlowConstructAware) targetSubFlowConstructAware).setFlowConstruct(flowConstruct);
    verify((MuleContextAware) targetMuleContextAwareAware).setMuleContext(muleContext);
  }

  @Test(expected = MuleRuntimeException.class)
  public void staticFlowRefDoesNotExist() throws Exception {
    when(expressionLanguage.isExpression(anyString())).thenReturn(false);

    getFlowRefProcessor(createFlowRefFactoryBean(NON_EXISTANT));
  }

  private Processor getFlowRefProcessor(FlowRefFactoryBean factoryBean) throws Exception {
    Processor processor = factoryBean.getObject();
    setMuleContextIfNeeded(processor, muleContext);
    return processor;
  }

  @Test(expected = MessagingException.class)
  public void dynamicFlowRefDoesNotExist() throws Exception {
    when(expressionLanguage.isExpression(anyString())).thenReturn(true);
    when(expressionLanguage.parse(eq(DYNAMIC_NON_EXISTANT), any(Event.class), any(FlowConstruct.class))).thenReturn("other");

    getFlowRefProcessor(createFlowRefFactoryBean(DYNAMIC_NON_EXISTANT)).process(testEvent());
  }

  private FlowRefFactoryBean createFlowRefFactoryBean(String name) throws InitialisationException {
    FlowRefFactoryBean flowRefFactoryBean = new FlowRefFactoryBean();
    flowRefFactoryBean.setName(name);
    flowRefFactoryBean.setApplicationContext(applicationContext);
    flowRefFactoryBean.setMuleContext(muleContext);
    flowRefFactoryBean.initialise();
    return flowRefFactoryBean;
  }

  private FlowRefFactoryBean createStaticFlowRefFactoryBean(Processor target) throws InitialisationException {
    when(expressionLanguage.isExpression(anyString())).thenReturn(false);
    when(applicationContext.getBean(eq(STATIC_REFERENCED_FLOW))).thenReturn(target);

    return createFlowRefFactoryBean(STATIC_REFERENCED_FLOW);
  }

  private FlowRefFactoryBean createDynamicFlowRefFactoryBean(Processor target) throws InitialisationException {
    when(expressionLanguage.isExpression(anyString())).thenReturn(true);
    when(expressionLanguage.parse(eq(DYNAMIC_REFERENCED_FLOW), any(Event.class), any(FlowConstruct.class)))
        .thenReturn(PARSED_DYNAMIC_REFERENCED_FLOW);
    when(applicationContext.getBean(eq(PARSED_DYNAMIC_REFERENCED_FLOW))).thenReturn(target);

    return createFlowRefFactoryBean(DYNAMIC_REFERENCED_FLOW);
  }

  private void verifyProcess(FlowRefFactoryBean flowRefFactoryBean, Processor target, int lifecycleRounds)
      throws Exception {
    assertSame(result, getFlowRefProcessor(flowRefFactoryBean).process(testEvent()));
    assertSame(result, getFlowRefProcessor(flowRefFactoryBean).process(testEvent()));

    verify(applicationContext).getBean(anyString());

    verify(target, times(2)).process(any(Event.class));
    verify((Initialisable) target, times(lifecycleRounds)).initialise();

    flowRefFactoryBean.dispose();
    verify((Disposable) target, times(lifecycleRounds)).dispose();
  }

}
