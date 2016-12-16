/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.introspection.enricher;

import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.addInterceptorFactory;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.extension.api.declaration.DescribingContext;
import org.mule.runtime.extension.api.model.property.ConnectivityModelProperty;
import org.mule.runtime.module.extension.internal.runtime.connectivity.ConnectionInterceptor;
import org.mule.runtime.module.extension.internal.util.IdempotentDeclarationWalker;

/**
 * Adds a {@link ConnectionInterceptor} to all {@link OperationModel operations} which contain the
 * {@link ConnectivityModelProperty}
 *
 * @since 4.0
 */
public final class ConnectionModelEnricher extends AbstractAnnotatedModelEnricher {

  @Override
  public void enrich(DescribingContext describingContext) {
    new IdempotentDeclarationWalker() {

      @Override
      protected void onOperation(OperationDeclaration declaration) {
        if (declaration.isRequiresConnection()) {
          addInterceptorFactory(declaration, ConnectionInterceptor::new);
        }
      }
    }.walk(describingContext.getExtensionDeclarer().getDeclaration());
  }
}