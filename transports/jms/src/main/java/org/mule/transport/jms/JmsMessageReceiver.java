/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.transport.jms;

import org.mule.api.MuleException;
import org.mule.api.MuleRuntimeException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.AbstractReceiverWorker;
import org.mule.transport.ConnectException;
import org.mule.transport.jms.filters.JmsSelectorFilter;
import org.mule.transport.jms.redelivery.RedeliveryHandler;
import org.mule.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.resource.spi.work.WorkException;

/**
 * Registers a single JmsMessage listener but uses a thread pool to process incoming
 * messages.
 * @deprecated use {@link org.mule.transport.jms.MultiConsumerJmsMessageReceiver} (set by default).
 */
@Deprecated
public class JmsMessageReceiver extends AbstractMessageReceiver implements MessageListener
{

    protected JmsConnector connector;
    protected RedeliveryHandler redeliveryHandler;
    protected MessageConsumer consumer;
    protected Session session;
    protected boolean startOnConnect = false;
    private final boolean topic;

    public JmsMessageReceiver(Connector connector, FlowConstruct flowConstruct, InboundEndpoint endpoint)
            throws CreateException
    {
        super(connector, flowConstruct, endpoint);
        this.connector = (JmsConnector) connector;
        topic = this.connector.getTopicResolver().isTopic(endpoint);

        try
        {
            redeliveryHandler = this.connector.getRedeliveryHandlerFactory().create();
            redeliveryHandler.setConnector(this.connector);
        }
        catch (Exception e)
        {
            throw new CreateException(e, this);
        }
    }

    protected void doConnect() throws Exception
    {
        createConsumer();
        if (startOnConnect)
        {
            doStart();
        }
    }

    protected void doDisconnect() throws Exception
    {
        closeConsumer();
    }

    public void onMessage(Message message)
    {
        try
        {
            getWorkManager().scheduleWork(new JmsWorker(message, this));
        }
        catch (WorkException e)
        {
            throw new MuleRuntimeException(e);
        }
    }

    @Override
    public boolean shouldConsumeInEveryNode()
    {
        return !this.topic;
    }

    protected  class JmsWorker extends AbstractReceiverWorker
    {
        public JmsWorker(Message message, AbstractMessageReceiver receiver)
        {
            super(new ArrayList(1), receiver);
            messages.add(message);
        }

        public JmsWorker(List messages, AbstractMessageReceiver receiver)
        {
            super(messages, receiver);
        }

        @Override
        protected Object preProcessMessage(Object message) throws Exception
        {
            Message m = (Message) message;

            if (logger.isDebugEnabled())
            {
                logger.debug("Message received it is of type: " +
                        ClassUtils.getSimpleName(message.getClass()));
                if (m.getJMSDestination() != null)
                {
                    logger.debug("Message received on " + m.getJMSDestination() + " ("
                            + m.getJMSDestination().getClass().getName() + ")");
                }
                else
                {
                    logger.debug("Message received on unknown destination");
                }
                logger.debug("Message CorrelationId is: " + m.getJMSCorrelationID());
                logger.debug("Jms Message Id is: " + m.getJMSMessageID());
            }

            if (m.getJMSRedelivered() && redeliveryHandler != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Message with correlationId: " + m.getJMSCorrelationID()
                            + " has redelivered flag set, handing off to Exception Handler");
                }
                redeliveryHandler.handleRedelivery(m, receiver.getEndpoint(), receiver.getFlowConstruct());
            }
            return m;

        }

        protected void bindTransaction(Transaction tx) throws TransactionException
        {
            if(tx instanceof JmsTransaction)
            {
                tx.bindResource(connector.getConnection(), ReusableSessionWrapperFactory.createWrapper(session));
            }
            else if(tx instanceof JmsClientAcknowledgeTransaction)
            {
                //We should still bind the session to the transaction, but we also need the message itself
                //since that is the object that gets Acknowledged

                tx.bindResource(connector.getConnection(), ReusableSessionWrapperFactory.createWrapper(session));
                ((JmsClientAcknowledgeTransaction)tx).setMessage((Message)messages.get(0));
            }
        }
    }

    protected void doStart() throws MuleException
    {
        try
        {
            // We ned to register the listener when start is called in order to only
            // start receiving messages after
            // start/
            // If the consumer is null it means that the connection strategy is being
            // run in a separate thread
            // And hasn't managed to connect yet.
            if (consumer == null)
            {
                startOnConnect = true;
            }
            else
            {
                startOnConnect = false;
                consumer.setMessageListener(this);
            }
        }
        catch (JMSException e)
        {
            throw new LifecycleException(e, this);
        }
    }

    protected void doStop() throws MuleException
    {
        super.doStop();

        try
        {
            if (consumer != null)
            {
                consumer.setMessageListener(null);
            }
        }
        catch (JMSException e)
        {
            throw new LifecycleException(e, this);
        }
    }

    protected void doDispose()
    {
        // template method
    }

    protected void closeConsumer()
    {
        connector.closeQuietly(consumer);
        consumer = null;
        connector.closeQuietly(session);
        session = null;
    }

    /**
     * Create a consumer for the jms destination
     *
     * @throws Exception
     */
    protected void createConsumer() throws Exception
    {
        try
        {
            JmsSupport jmsSupport = this.connector.getJmsSupport();
            // Create session if none exists
            if (session == null)
            {
                session = this.connector.getSession(endpoint);
            }

            // Create destination
            Destination dest = jmsSupport.createDestination(session, endpoint);

            // Extract jms selector
            String selector = null;
            JmsSelectorFilter selectorFilter = connector.getSelector(endpoint);
            if (selectorFilter != null)
            {
                selector = selectorFilter.getExpression();
            }
            else if (endpoint.getProperties() != null)
            {
                // still allow the selector to be set as a property on the endpoint
                // to be backward compatable
                selector = (String) endpoint.getProperties().get(JmsConstants.JMS_SELECTOR_PROPERTY);
            }
            String tempDurable = (String) endpoint.getProperties().get(JmsConstants.DURABLE_PROPERTY);
            boolean durable = connector.isDurable();
            if (tempDurable != null)
            {
                durable = Boolean.valueOf(tempDurable).booleanValue();
            }

            // Get the durable subscriber name if there is one
            String durableName = (String) endpoint.getProperties().get(JmsConstants.DURABLE_NAME_PROPERTY);
            if (durableName == null && durable && dest instanceof Topic)
            {
                durableName = "mule." + connector.getName() + "." + endpoint.getEndpointURI().getAddress();
                logger.debug("Jms Connector for this receiver is durable but no durable name has been specified. Defaulting to: "
                        + durableName);
            }

            // Create consumer
            consumer = jmsSupport.createConsumer(session, dest, selector, connector.isNoLocal(), durableName,
                    topic, endpoint);
        }
        catch (JMSException e)
        {
            throw new ConnectException(e, this);
        }
    }

}
