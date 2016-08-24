/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.ws.consumer;

import org.junit.Ignore;
import org.junit.Test;
import org.mule.runtime.core.api.MuleException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.size.SmallTest;

@SmallTest
public class WSConsumerConfigTestCase extends AbstractMuleContextTestCase {

  private static final String SERVICE_ADDRESS = "http://localhost";

  @Ignore("See MULE-9210")
  @Test(expected = MuleException.class)
  public void failToCreateOutboundEndpointWithUnsupportedProtocol() throws Exception {
    WSConsumerConfig config = createConsumerConfig();
    config.setServiceAddress("unsupported://test");
    config.createOutboundMessageProcessor(getTestFlow());
  }

  @Ignore("See MULE-9210")
  @Test(expected = IllegalStateException.class)
  public void failToCreateOutboundEndpointWithWrongConnector() throws MuleException {
    // TODO implement test case once MULE-9210 is solved
  }

  @Test(expected = IllegalStateException.class)
  public void failToCreateOutboundEndpointWithEmptyServiceAddress() throws Exception {
    WSConsumerConfig config = createConsumerConfig();
    config.setServiceAddress(null);
    config.createOutboundMessageProcessor(getTestFlow());
  }

  private WSConsumerConfig createConsumerConfig() {
    WSConsumerConfig config = new WSConsumerConfig();
    config.setMuleContext(muleContext);
    config.setWsdlLocation("TestWsdlLocation");
    config.setServiceAddress(SERVICE_ADDRESS);
    config.setService("TestService");
    config.setPort("TestPort");
    return config;
  }

}
