/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.cxf.support;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessage;

public class ComponentNotFoundRuntimeException extends MuleRuntimeException {

  public ComponentNotFoundRuntimeException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public ComponentNotFoundRuntimeException(I18nMessage message) {
    super(message);
  }

}


