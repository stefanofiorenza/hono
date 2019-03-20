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

import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

/**
 * Defines how to serialize and de-serialize a RegistrationKey for the infinispan hotrod client.
 * The protobuf schema is defined in resources/registry.proto.
 *
 */
public class RegistrationKeyMarshaller implements MessageMarshaller<RegistrationKey> {

    @Override
    public String getTypeName() {
        return "registry.RegistrationKey";
    }

    @Override
    public Class<? extends RegistrationKey> getJavaClass() {
        return RegistrationKey.class;
    }

    @Override
    public void writeTo(final ProtoStreamWriter writer, final RegistrationKey obj) throws IOException {
        writer.writeString("tenantId", obj.tenantId);
        writer.writeString("deviceId", obj.deviceId);
    }

    @Override
    public RegistrationKey readFrom(final ProtoStreamReader reader) throws IOException {

        final String tenantId = reader.readString("tenantId");
        final String deviceId = reader.readString("deviceId");
        return new RegistrationKey(tenantId, deviceId);
    }
}
