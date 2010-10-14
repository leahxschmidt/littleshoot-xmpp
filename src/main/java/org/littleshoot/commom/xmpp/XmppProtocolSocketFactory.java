package org.littleshoot.commom.xmpp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmppProtocolSocketFactory implements ProtocolSocketFactory {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final XmppSocketFactory xmppSocketFactory;
    private final DefaultXmppUriFactory xmppUriFactory;
    
    public XmppProtocolSocketFactory(final XmppSocketFactory xmppSocketFactory,
        final DefaultXmppUriFactory defaultXmppUriFactory) {
        this.xmppSocketFactory = xmppSocketFactory;
        this.xmppUriFactory = defaultXmppUriFactory;
    }

    public Socket createSocket(final String host, final int port, 
        final InetAddress localAddress, final int localPort) 
        throws IOException, UnknownHostException {
        log.warn("Attempted unsupported socket call");
        throw new UnsupportedOperationException("not allowed");
    }

    public Socket createSocket(final String host, final int port, 
        final InetAddress localAddress, final int localPort, 
        final HttpConnectionParams params) throws IOException,
        UnknownHostException, ConnectTimeoutException {
        
        return null;
    }

    public Socket createSocket(final String host, final int port) 
        throws IOException, UnknownHostException {
        log.trace("Creating a socket for user: {}", host);
        final Preferences prefs = Preferences.userRoot();
        final long id = prefs.getLong("LITTLESHOOT_ID", -1);
        if (id == Long.parseLong(host)) {
            // This is an error because we should just stream it locally
            // if we have the file (we're trying to connect to ourselves!).
            log.error("Ignoring request to download from ourselves...");
            throw new IOException("Not downloading from ourselves...");
        }
        
        final URI uri = this.xmppUriFactory.createXmppUri(host);
        try {
            log.trace("About to create socket...");
            final Socket sock = this.xmppSocketFactory.newSocket(uri);
            log.debug("Got socket!! Returning to HttpClient");
            
            // Note there can appear to be an odd delay after this point if
            // you're just looking at the raw logs, but it's due to HttpClient
            // actually making the HTTP request and getting a response.
            return sock;
        }
        catch (final IOException e) {
            log.warn("Exception creating SIP socket", e);
            throw e;
        }
    }
}
