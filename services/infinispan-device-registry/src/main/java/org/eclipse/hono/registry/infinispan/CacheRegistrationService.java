/*******************************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.hono.registry.infinispan;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.eclipse.hono.service.registration.CompleteBaseRegistrationService;
import org.eclipse.hono.util.RegistrationResult;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.net.HttpURLConnection;

/**
 * A Registration service that use an Infinispan as a backend service.
 * Infinispan is an open source project providing a distributed in-memory key/value data store
 *
 * <p>
 *@see <a href="https://infinspan.org">https://infinspan.org</a>
 *
 */
@Repository
@Primary
public class CacheRegistrationService extends CompleteBaseRegistrationService<CacheRegistrationConfigProperties> {

    private final RemoteCache<RegistrationKey, String> registrationCache;

    @Autowired
    protected CacheRegistrationService(final RemoteCacheManager cacheManager) {
        this.registrationCache = cacheManager.administration().getOrCreateCache("registration", "default");
        //this.registrationCache = cacheManager.getCache("registration");
    }

    @Override
    public void setConfig(final CacheRegistrationConfigProperties configuration) {
    }

    @Override
    public void addDevice(final String tenantId, final String deviceId, final JsonObject otherKeys, final Handler<AsyncResult<RegistrationResult>> resultHandler) {

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);

        registrationCache.putIfAbsentAsync(key, otherKeys.encode()).thenAccept(result -> {
            if ( result == null){
                    resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_CREATED)));
            } else {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_CONFLICT)));
            }
        });
    }

    @Override
    public void updateDevice(final String tenantId, final String deviceId, final JsonObject otherKeys, final Handler<AsyncResult<RegistrationResult>> resultHandler) {

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);
        registrationCache.replaceAsync(key, otherKeys.encode()).thenAccept( result -> {
            if ( result == null){
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
            }
        });
    }

    @Override
    public void removeDevice(final String tenantId, final String deviceId, final Handler<AsyncResult<RegistrationResult>> resultHandler) {

        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);
        registrationCache.removeAsync(key).thenAccept( result -> {
            if ( result == null){
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NO_CONTENT)));
            }
        });
    }

    @Override
    public void getDevice(final String tenantId, final String deviceId, final Handler<AsyncResult<RegistrationResult>> resultHandler) {
        final RegistrationKey key = new RegistrationKey(tenantId, deviceId);

        registrationCache.getAsync(key).thenAccept( result -> {
            if ( result == null){
                resultHandler.handle(Future.succeededFuture(RegistrationResult.from(HttpURLConnection.HTTP_NOT_FOUND)));
            } else {
                resultHandler.handle(Future.succeededFuture(
                        RegistrationResult.from(HttpURLConnection.HTTP_OK, getResultPayload(deviceId, new JsonObject(result)))));
            }
        });
    }
}
