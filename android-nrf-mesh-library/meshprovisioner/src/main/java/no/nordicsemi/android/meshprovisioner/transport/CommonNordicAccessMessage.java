package no.nordicsemi.android.meshprovisioner.transport;

import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class CommonNordicAccessMessage extends GenericMessage {
    public CommonNordicAccessMessage(ApplicationKey appKey, int opCode, byte[] params) {
        super(appKey);
        this.opCode = opCode;
        this.mParameters = params;
    }

    int opCode;
    @Override
    int getOpCode() {
        return opCode;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }
}
