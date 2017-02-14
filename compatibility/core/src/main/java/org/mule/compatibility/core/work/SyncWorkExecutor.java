/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.core.work;

import org.mule.compatibility.core.api.work.WorkExecutor;

import java.util.concurrent.Executor;

import javax.resource.spi.work.WorkException;

public class SyncWorkExecutor implements WorkExecutor {

  @Override
  public void doExecute(WorkerContext work, Executor executor) throws WorkException, InterruptedException {
    work.run();
  }

}