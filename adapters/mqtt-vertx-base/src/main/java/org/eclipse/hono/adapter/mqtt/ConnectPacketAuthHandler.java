/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */


package org.eclipse.hono.adapter.mqtt;

import java.net.HttpURLConnection;

import org.eclipse.hono.client.ClientErrorException;
import org.eclipse.hono.client.ServiceInvocationException;
import org.eclipse.hono.service.auth.device.ExecutionContextAuthHandler;
import org.eclipse.hono.service.auth.device.HonoClientBasedAuthProvider;
import org.eclipse.hono.service.auth.device.UsernamePasswordCredentials;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttAuth;


/**
 * An auth handler for extracting a username and password from an MQTT CONNECT packet.
 *
 */
public class ConnectPacketAuthHandler extends ExecutionContextAuthHandler<MqttContext> {

    /**
     * Creates a new handler for a Hono client based auth provider.
     * 
     * @param authProvider The provider to use for verifying a device's credentials.
     */
    public ConnectPacketAuthHandler(final HonoClientBasedAuthProvider<UsernamePasswordCredentials> authProvider) {
        super(authProvider);
    }

    /**
     * Extracts credentials from a client's MQTT <em>CONNECT</em> packet.
     * <p>
     * The JSON object returned will contain a <em>username</em> and a
     * <em>password</em> property with values extracted from the corresponding
     * fields of the <em>CONNECT</em> packet.
     *
     * @param context The MQTT context for the client's CONNECT packet.
     * @return A future indicating the outcome of the operation.
     *         The future will succeed with the client's credentials extracted from the CONNECT packet
     *         or it will fail with a {@link ServiceInvocationException} indicating the cause of the failure.
     * @throws IllegalArgumentException if the context does not contain an MQTT endpoint.
     */
    @Override
    public Future<JsonObject> parseCredentials(final MqttContext context) {


        if (context.deviceEndpoint() == null) {
            throw new IllegalArgumentException("no device endpoint");
        }

        final Future<JsonObject> result = Future.future();
        final MqttAuth auth = context.deviceEndpoint().auth();

        if (auth == null) {

            result.fail(new ClientErrorException(
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    "device did not provide credentials in CONNECT packet"));

        } else if (auth.getUsername() == null || auth.getPassword() == null) {

            result.fail(new ClientErrorException(
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    "device provided malformed credentials in CONNECT packet"));

        } else {
            result.complete(new JsonObject()
                    .put("username", auth.getUsername())
                    .put("password", auth.getPassword()));
        }

        return result;
    }
}
