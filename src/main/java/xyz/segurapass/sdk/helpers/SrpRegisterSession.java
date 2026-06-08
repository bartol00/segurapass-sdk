package xyz.segurapass.sdk.helpers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public final class SrpRegisterSession implements AutoCloseable {

    private byte[] passwordBytes;
    private byte[] vaultKey;
    private byte[] saltAuth;
    private byte[] saltKey;
    private byte[] vaultKeyIv;

    private SrpRegisterSession() {}

    public static SrpRegisterSession create(SecureRandom random, char[] masterPassword) {
        SrpRegisterSession ctx = new SrpRegisterSession();

        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(masterPassword));
        ctx.passwordBytes = new byte[bb.remaining()];
        bb.get(ctx.passwordBytes);

        ctx.vaultKey = new byte[32];
        ctx.saltAuth = new byte[16];
        ctx.saltKey = new byte[16];
        ctx.vaultKeyIv = new byte[12];

        random.nextBytes(ctx.vaultKey);
        random.nextBytes(ctx.saltAuth);
        random.nextBytes(ctx.saltKey);
        random.nextBytes(ctx.vaultKeyIv);

        return ctx;
    }

    public byte[] passwordBytes() { return passwordBytes; }
    public byte[] vaultKey() { return vaultKey; }
    public byte[] saltAuth() { return saltAuth; }
    public byte[] saltKey() { return saltKey; }
    public byte[] vaultKeyIv() { return vaultKeyIv; }

    @Override
    public void close() {
        wipe(passwordBytes);
        wipe(vaultKey);
        wipe(saltAuth);
        wipe(saltKey);
        wipe(vaultKeyIv);

        passwordBytes = null;
        vaultKey = null;
        saltAuth = null;
        saltKey = null;
        vaultKeyIv = null;
    }

    private void wipe(byte[] arr) {
        if (arr != null) Arrays.fill(arr, (byte) 0);
    }
}