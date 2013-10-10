/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.transport.xmpp;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.transport.ConnectException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;


public abstract class AbstractXmppConversation implements XmppConversation
{
    protected final Log logger = LogFactory.getLog(getClass());

    protected XMPPConnection connection;
    protected String recipient;
    protected PacketCollector packetCollector;

    public AbstractXmppConversation(ImmutableEndpoint endpoint)
    {
        super();
        connection = ((XmppConnector) endpoint.getConnector()).getXmppConnection();
        recipient = XmppConnector.getRecipient(endpoint);
    }

    @Override
    public void connect() throws ConnectException
    {
        doConnect();
        packetCollector = createPacketCollector();
    }

    /**
     * Subclasses can override this method to create their conversation specific connection.
     */
    protected void doConnect() throws ConnectException
    {
        // template method
    }

    /**
     * @return a {@link PacketCollector} that can be used to retrieve messages for this
     * conversation.
     */
    protected PacketCollector createPacketCollector()
    {
        PacketFilter filter = createPacketFilter();
        return connection.createPacketCollector(filter);
    }

    /**
     * @return a {@link PacketFilter} instance that matches the desired message type and recipient
     * for this conversation.
     */
    protected PacketFilter createPacketFilter()
    {
        return null;
    }

    @Override
    public void disconnect()
    {
        if (packetCollector != null)
        {
            packetCollector.cancel();
        }

        doDisconnect();
    }

    /**
     * Subclasses can override this method to perform custom disconnect actions.
     */
    protected void doDisconnect()
    {
        // template method
    }

    @Override
    public Message receive(long timeout)
    {
        // The filter of our packetCollector should make sure that we receive only
        // Message instances here
        return (Message) packetCollector.nextResult(timeout);
    }

    @Override
    public Message receive()
    {
        // The filter of our packetCollector should make sure that we receive only
        // Message instances here
        return (Message) packetCollector.nextResult();
    }
}
