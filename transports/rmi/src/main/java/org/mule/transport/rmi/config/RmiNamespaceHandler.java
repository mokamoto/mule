/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.transport.rmi.config;

import org.mule.api.config.MuleProperties;
import org.mule.config.spring.factories.InboundEndpointFactoryBean;
import org.mule.config.spring.factories.OutboundEndpointFactoryBean;
import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.endpoint.TransportEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportGlobalEndpointDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.rmi.RmiConnector;

/**
 * Registers a Bean Definition Parser for handling <code>&lt;rmi:connector&gt;</code> elements.
 *
 */
public class RmiNamespaceHandler extends AbstractMuleNamespaceHandler
{

    public static final String OBJECT = "object";
    public static final String[] PROPERTIES = new String[]{MuleProperties.MULE_METHOD_PROPERTY}; // , RmiConnector.PROPERTY_SERVICE_METHOD_PARAM_TYPES};
    public static final String[] ADDRESS = new String[]{OBJECT, URIBuilder.HOST, URIBuilder.PORT};

    public void init()
    {
        registerMuleBeanDefinitionParser("endpoint", new TransportGlobalEndpointDefinitionParser(RmiConnector.RMI, TransportGlobalEndpointDefinitionParser.PROTOCOL, ADDRESS, PROPERTIES)).addAlias(OBJECT, URIBuilder.PATH);
        registerMuleBeanDefinitionParser("inbound-endpoint", new TransportEndpointDefinitionParser(RmiConnector.RMI, TransportGlobalEndpointDefinitionParser.PROTOCOL, InboundEndpointFactoryBean.class, ADDRESS, PROPERTIES)).addAlias(OBJECT, URIBuilder.PATH);
        registerMuleBeanDefinitionParser("outbound-endpoint", new TransportEndpointDefinitionParser(RmiConnector.RMI, TransportGlobalEndpointDefinitionParser.PROTOCOL, OutboundEndpointFactoryBean.class, ADDRESS, PROPERTIES)).addAlias(OBJECT, URIBuilder.PATH);
        registerConnectorDefinitionParser(RmiConnector.class);
    }

}
