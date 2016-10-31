/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.mule.functional.extensions.CompatibilityFunctionalTestCase;
import org.mule.functional.functional.FlowAssert;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleEventContext;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.lifecycle.Callable;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.ClassRule;
import org.junit.Test;

/**
 * Tests to validate that MuleClient can be used from MessageProcessor and JavaComponent in order to dispatch an event to a
 * sub-flow, without losing the Session variables.
 */
public class MuleClientDispatchWithoutLosingSessionVariablesTestCase extends CompatibilityFunctionalTestCase {

  @ClassRule
  public static DynamicPort port = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "org/mule/test/integration/client/client-session-vars-when-dispatch-flow.xml";
  }

  private void doSendMessageToHttp(String flowName) throws Exception {
    InternalMessage result = flowRunner(flowName).withPayload("TEST1").run().getMessage();
    assertThat(result, notNullValue(InternalMessage.class));
    FlowAssert.verify(flowName);
  }

  @Test
  public void testSessionVarsAfterDispatchFromMessageProcessor() throws Exception {
    doSendMessageToHttp("sessionVarsFlowUsingProcessor");
  }

  @Test
  public void testSessionVarsAfterDispatchFromJavaComponent() throws Exception {
    doSendMessageToHttp("sessionVarsFlowUsingJavaComponent");
  }

  @Test
  public void testSessionVarsFlowUsingJavaComponentRequestResponse() throws Exception {
    doSendMessageToHttp("sessionVarsFlowUsingJavaComponentRequestResponse");
  }

  public static class MessageProcessorDispatchFlowUsingNewMuleClient implements Processor {

    @Override
    public Event process(Event event) throws MuleException {
      muleContext.getClient().dispatch(getUrl("innertest"), InternalMessage.builder().payload("payload").build());
      return event;

    }
  }

  public static class JavaComponentDispatchFlowUsingNewMuleClient implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
      muleContext.getClient().dispatch(getUrl("innertest"), InternalMessage.builder().payload("payload").build());
      return eventContext.getMessage();
    }
  }

  public static class JavaComponentSendFlowUsingNewMuleClient implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
      muleContext.getClient().send(getUrl("innerrequestresponsetest"), InternalMessage.builder().payload("payload").build());
      return eventContext.getMessage();
    }
  }

  private static String getUrl(String path) {
    return String.format("http://localhost:%s/%s", port.getValue(), path);
  }

}
