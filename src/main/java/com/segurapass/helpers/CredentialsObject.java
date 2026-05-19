package com.segurapass.helpers;

import java.util.Arrays;

public final class CredentialsObject implements AutoCloseable {

    private byte[] vaultKey;

    public CredentialsObject(byte[] vaultKey) {
        this.vaultKey = vaultKey;
    }

    public byte[] vaultKey() {
        return vaultKey;
    }

    @Override
    public void close() {
        if (vaultKey != null) {
            Arrays.fill(vaultKey, (byte) 0);
            vaultKey = null;
        }
    }
}
