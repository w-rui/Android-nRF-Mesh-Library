/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.meshprovisioner.provisionerstates;


import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class ProvisioningPublicKeyState extends ProvisioningState {

    private static final int PROVISIONING_PUBLIC_KEY_XY_PDU_LENGTH = 69;
    private final String TAG = ProvisioningPublicKeyState.class.getSimpleName();
    private final byte[] publicKeyXY = new byte[PROVISIONING_PUBLIC_KEY_XY_PDU_LENGTH];
    private final MeshProvisioningStatusCallbacks mStatusCallbacks;
    protected final UnprovisionedMeshNode mUnprovisionedMeshNode;
    private final InternalTransportCallbacks mInternalTransportCallbacks;

    protected byte[] mTempProvisioneeXY;
    protected int segmentCount = 0;
    protected Object mProvisionerPrivaetKey;


    public ProvisioningPublicKeyState(final UnprovisionedMeshNode unprovisionedMeshNode, final InternalTransportCallbacks mInternalTransportCallbacks, final MeshProvisioningStatusCallbacks meshProvisioningStatusCallbacks) {
        super();
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
        this.mStatusCallbacks = meshProvisioningStatusCallbacks;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_PUBLIC_KEY;
    }

    @Override
    public void executeSend() {
        generateKeyPairs();
        final byte[] pdu = generatePublicKeyXYPDU();
        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, States.PROVISIONING_PUBLIC_KEY_SENT, pdu);
        mInternalTransportCallbacks.sendProvisioningPdu(mUnprovisionedMeshNode, pdu);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, States.PROVISIONING_PUBLIC_KEY_RECEIVED, data);
        generateSharedECDHSecret(data);
        return true;
    }

    protected void generateKeyPairs() {

        try {
            final byte[] tempX = null;
            final byte[] tempY = null;

            Log.v(TAG, "X: length: " + tempX.length + " " + MeshParserUtils.bytesToHex(tempX, false));
            Log.v(TAG, "Y: length: " + tempY.length + " " + MeshParserUtils.bytesToHex(tempY, false));

            final byte[] tempXY = new byte[64];
            System.arraycopy(tempX, 0, tempXY, 0, tempX.length);
            System.arraycopy(tempY, 0, tempXY, tempY.length, tempY.length);

            if (true) throw new RuntimeException("Should implement with J2OBJC.");
            mUnprovisionedMeshNode.setProvisionerPublicKeyXY(tempXY);

            Log.v(TAG, "XY: " + MeshParserUtils.bytesToHex(tempXY, true));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] generatePublicKeyXYPDU() {

        final byte[] tempXY = mUnprovisionedMeshNode.getProvisionerPublicKeyXY();

        ByteBuffer buffer = ByteBuffer.allocate(tempXY.length + 2);
        buffer.put(MeshManagerApi.PDU_TYPE_PROVISIONING);
        buffer.put(TYPE_PROVISIONING_PUBLIC_KEY);
        buffer.put(tempXY);

        return buffer.array();
    }

    protected void generateSharedECDHSecret(final byte[] provisioneePublicKeyXYPDU) {
        if (provisioneePublicKeyXYPDU.length != 66) {
            throw new IllegalArgumentException("Invalid Provisionee Public Key PDU," +
                    " length of the Provisionee public key must be 66 bytes, but was " + provisioneePublicKeyXYPDU.length);
        }
        final ByteBuffer buffer = ByteBuffer.allocate(provisioneePublicKeyXYPDU.length - 2);
        buffer.put(provisioneePublicKeyXYPDU, 2, buffer.limit());
        final byte[] xy = mTempProvisioneeXY = buffer.array();
        mUnprovisionedMeshNode.setProvisioneePublicKeyXY(xy);

        final byte[] xComponent = new byte[32];
        System.arraycopy(xy, 0, xComponent, 0, xComponent.length);

        final byte[] yComponent = new byte[32];
        System.arraycopy(xy, 32, yComponent, 0, xComponent.length);

        final byte[] provisioneeX = convertToLittleEndian(xComponent, ByteOrder.LITTLE_ENDIAN);
        Log.v(TAG, "Provsionee X: " + MeshParserUtils.bytesToHex(provisioneeX, false));

        final byte[] provisioneeY = convertToLittleEndian(yComponent, ByteOrder.LITTLE_ENDIAN);
        Log.v(TAG, "Provsionee Y: " + MeshParserUtils.bytesToHex(provisioneeY, false));

        final byte[] sharedECDHSecret = null;
        if (true) throw new RuntimeException("Should implement with J2OBJC.");

        mUnprovisionedMeshNode.setSharedECDHSecret(sharedECDHSecret);
        Log.v(TAG, "ECDH Secret: " + MeshParserUtils.bytesToHex(sharedECDHSecret, false));
    }

    private byte[] convertToLittleEndian(final byte[] data, final ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.order(order);
        buffer.put(data);
        return buffer.array();
    }
}
