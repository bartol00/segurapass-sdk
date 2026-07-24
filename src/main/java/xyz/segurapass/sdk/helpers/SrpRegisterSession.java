package xyz.segurapass.sdk.helpers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;

public final class SrpRegisterSession implements AutoCloseable {

    private byte[] passwordBytes;
    private byte[] saltAuth;
    private byte[] saltKey;
    private byte[] saltHkdf;
    private byte[] vaultKey;
    private byte[] vaultKeyIv;
    private byte[] privateSigningKey;
    private byte[] publicSigningKey;
    private byte[] privateSigningKeyIv;

    private SrpRegisterSession() {}

    public static SrpRegisterSession create(SecureRandom random, char[] masterPassword)
            throws Exception
    {
        SrpRegisterSession ctx = new SrpRegisterSession();

        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(masterPassword));
        ctx.passwordBytes = new byte[bb.remaining()];
        bb.get(ctx.passwordBytes);

        KeyPair signingKeypair = EncryptionHelper.generateSigningKeypair();

        ctx.saltAuth = new byte[16];
        ctx.saltKey = new byte[16];
        ctx.saltHkdf = new byte[16];
        ctx.vaultKey = new byte[32];
        ctx.vaultKeyIv = new byte[12];
        ctx.privateSigningKey = EncryptionHelper.getPrivateSigningKeyBytes(signingKeypair.getPrivate());
        ctx.publicSigningKey = EncryptionHelper.getPublicSigningKeyBytes(signingKeypair.getPublic());
        ctx.privateSigningKeyIv = new byte[12];

        random.nextBytes(ctx.vaultKey);
        random.nextBytes(ctx.saltAuth);
        random.nextBytes(ctx.saltKey);
        random.nextBytes(ctx.saltHkdf);
        random.nextBytes(ctx.vaultKeyIv);
        random.nextBytes(ctx.privateSigningKeyIv);

        return ctx;
    }

    public byte[] passwordBytes() { return passwordBytes; }
    public byte[] saltAuth() { return saltAuth; }
    public byte[] saltKey() { return saltKey; }
    public byte[] saltHkdf() { return saltHkdf; }
    public byte[] vaultKey() { return vaultKey; }
    public byte[] vaultKeyIv() { return vaultKeyIv; }
    public byte[] privateSigningKey() { return privateSigningKey; }
    public byte[] publicSigningKey() { return publicSigningKey; }
    public byte[] privateSigningKeyIv() { return privateSigningKeyIv; }

    @Override
    public void close() {
        wipe(passwordBytes);
        wipe(saltAuth);
        wipe(saltKey);
        wipe(saltHkdf);
        wipe(vaultKey);
        wipe(vaultKeyIv);
        wipe(privateSigningKey);
        wipe(publicSigningKey);
        wipe(privateSigningKeyIv);

        passwordBytes = null;
        vaultKey = null;
        saltAuth = null;
        saltKey = null;
        saltHkdf = null;
        vaultKeyIv = null;
        privateSigningKey = null;
        publicSigningKey = null;
        privateSigningKeyIv = null;
    }

    private void wipe(byte[] arr) {
        if (arr != null) Arrays.fill(arr, (byte) 0);
    }
}