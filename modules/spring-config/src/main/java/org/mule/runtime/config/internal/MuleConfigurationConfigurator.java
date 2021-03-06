/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.internal;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.config.MuleDeploymentProperties.MULE_LAZY_INIT_DEPLOYMENT_PROPERTY;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.ConfigurationProperties;
import org.mule.runtime.api.serialization.ObjectSerializer;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.config.ConfigurationExtension;
import org.mule.runtime.core.api.config.DefaultMuleConfiguration;
import org.mule.runtime.core.api.config.DynamicConfigExpiration;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.core.internal.context.DefaultMuleContext;
import org.mule.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.SmartFactoryBean;

/**
 * This class is a "SmartFactoryBean" which allows a few XML attributes to be set on the otherwise read-only MuleConfiguration. It
 * looks up the MuleConfiguration from the MuleContext and does some class-casting to be able to modify it. Note that this will
 * only work if the MuleContext has not yet been started, otherwise the modifications will be ignored (and warnings logged).
 */
// TODO MULE-9638 remove usage of SmartFactoryBean
public class MuleConfigurationConfigurator extends AbstractComponentFactory implements SmartFactoryBean {

  @Inject
  private MuleContext muleContext;

  @Inject
  private Registry registry;

  @Inject
  private ConfigurationProperties configurationProperties;

  // We instantiate DefaultMuleConfiguration to make sure we get the default values for
  // any properties not set by the user.
  private DefaultMuleConfiguration config = new DefaultMuleConfiguration();

  @Override
  public boolean isEagerInit() {
    return true;
  }

  @Override
  public boolean isPrototype() {
    return false;
  }

  private void applyDefaultIfNoObjectSerializerSet(DefaultMuleConfiguration configuration) {
    ObjectSerializer configuredSerializer = config.getDefaultObjectSerializer();

    if (configuredSerializer != null) {
      configuration.setDefaultObjectSerializer(configuredSerializer);
      if (muleContext instanceof DefaultMuleContext) {
        ((DefaultMuleContext) muleContext).setObjectSerializer(configuredSerializer);
      }
    }
  }

  @Override
  public Class<?> getObjectType() {
    return MuleConfiguration.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setDefaultSynchronousEndpoints(boolean synchronous) {
    config.setDefaultSynchronousEndpoints(synchronous);
  }

  public void setDefaultResponseTimeout(int responseTimeout) {
    config.setDefaultResponseTimeout(responseTimeout);
  }

  public void setDefaultTransactionTimeout(int defaultTransactionTimeout) {
    config.setDefaultTransactionTimeout(defaultTransactionTimeout);
  }

  public void setShutdownTimeout(int shutdownTimeout) {
    config.setShutdownTimeout(shutdownTimeout);
  }

  public void setDefaultErrorHandlerName(String defaultErrorHandlerName) {
    config.setDefaultErrorHandlerName(defaultErrorHandlerName);
  }

  public void setDefaultObjectSerializer(ObjectSerializer objectSerializer) {
    config.setDefaultObjectSerializer(objectSerializer);
  }

  public void setMaxQueueTransactionFilesSize(int queueTransactionFilesSizeInMegabytes) {
    config.setMaxQueueTransactionFilesSize(queueTransactionFilesSizeInMegabytes);
  }

  public void setDynamicConfigExpiration(DynamicConfigExpiration dynamicConfigExpiration) {
    config.setDynamicConfigExpiration(dynamicConfigExpiration);
  }

  public void setExtensions(List<ConfigurationExtension> extensions) {
    config.addExtensions(extensions);
  }

  @Override
  public Object doGetObject() throws Exception {
    MuleConfiguration configuration = muleContext.getConfiguration();
    if (configuration instanceof DefaultMuleConfiguration) {
      DefaultMuleConfiguration defaultConfig = (DefaultMuleConfiguration) configuration;
      // First of all set the lazyInit mode or not to allow modifications to the configuration
      defaultConfig.setLazyInit(configurationProperties.resolveBooleanProperty(MULE_LAZY_INIT_DEPLOYMENT_PROPERTY).orElse(false));

      defaultConfig.setDefaultResponseTimeout(config.getDefaultResponseTimeout());
      defaultConfig.setDefaultTransactionTimeout(config.getDefaultTransactionTimeout());
      defaultConfig.setShutdownTimeout(config.getShutdownTimeout());
      defaultConfig.setDefaultErrorHandlerName(config.getDefaultErrorHandlerName());
      defaultConfig.addExtensions(config.getExtensions());
      defaultConfig.setMaxQueueTransactionFilesSize(config.getMaxQueueTransactionFilesSizeInMegabytes());
      defaultConfig.setDynamicConfigExpiration(config.getDynamicConfigExpiration());
      applyDefaultIfNoObjectSerializerSet(defaultConfig);

      return configuration;
    } else {
      throw new ConfigurationException(createStaticMessage("Unable to set properties on read-only MuleConfiguration: "
          + configuration.getClass()));
    }
  }

}
