/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.admin.websocket;

import org.eclipse.jetty.websocket.WebSocket;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Used for publishing log messages to connected clients via the web socket api.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3
 */
public class LoggingWebSocket implements WebSocket.OnTextMessage {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(LoggingWebSocket.class);

    /**
     * Web Socket connections
     * TODO MM thread safe
     */
    private List<Connection> connections = new ArrayList<Connection>();

    /**
     * Default constructor.
     */
    public LoggingWebSocket() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        };
        timer.schedule(task, 60000, 60000);
    }

    /**
     * {@inheritDoc}
     */
    public void onOpen(Connection connection) {
        LOG.info("Accepted a new connection");
        this.connections.add(connection);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"PMD.CloseResource"})
    public void onClose(int closeCode, String message) {
        LOG.debug("Web socket connection closed");
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection == null || !connection.isOpen()) {
                itor.remove();
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public void onMessage(String data) {
        LOG.info("Received web socket client message: " + data);
    }

    /**
     * Send ping event.
     */
    @SuppressWarnings("unchecked")
    public void ping() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", SocketEvent.PING.name());
        push(jsonObject);
    }

    /**
     * Push event to connected clients.
     * @param event
     */
    @SuppressWarnings({"PMD.CloseResource"})
    protected void push(JSONObject event) {
        Iterator<Connection> itor = connections.iterator();
        while (itor.hasNext()) {
            Connection connection = itor.next();
            if (connection != null && connection.isOpen()) {
                try {
                    connection.sendMessage(event.toString());
                } catch (IOException e) {
                    LOG.error("Error sending message", e);
                }
            } else {
                itor.remove();
            }
        }
    }
}
