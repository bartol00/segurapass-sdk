package xyz.segurapass.sdk.helpers;

import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public final class PasswordChangeObject implements AutoCloseable {

    private byte[] passwordBytes;
    private byte[] vaultKeyBytes;
    private BigInteger a;
    private BigInteger A;
    private byte[] newSaltAuth;
    private byte[] newVaultKeyIv;
    private byte[] newSaltKey;
    private byte[] newPasswordBytes;

    public static PasswordChangeObject create(
            SecureRandom random,
            char[] password,
            char[] newPassword,
            byte[] vaultKeyBytes,
            SRP6GroupParameters group
    ) {
        PasswordChangeObject object = new PasswordChangeObject();

        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        object.passwordBytes = new byte[bb.remaining()];
        bb.get(object.passwordBytes);

        ByteBuffer newBb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(newPassword));
        object.newPasswordBytes = new byte[newBb.remaining()];
        newBb.get(object.newPasswordBytes);

        object.vaultKeyBytes = vaultKeyBytes;

        object.a = new BigInteger(256, random);
        object.A = group.getG().modPow(object.a, group.getN());

        object.newSaltAuth = new byte[16];
        object.newSaltKey = new byte[16];
        object.newVaultKeyIv = new byte[12];
        random.nextBytes(object.newSaltAuth);
        random.nextBytes(object.newSaltKey);
        random.nextBytes(object.newVaultKeyIv);

        return object;
    }

    @Override
    public void close() {
        if (passwordBytes != null) Arrays.fill(passwordBytes, (byte) 0);
        passwordBytes = null;

        if (newPasswordBytes != null) Arrays.fill(newPasswordBytes, (byte) 0);
        newPasswordBytes = null;

        if (vaultKeyBytes != null) Arrays.fill(vaultKeyBytes, (byte) 0);
        vaultKeyBytes = null;

        a = null;
        A = null;
    }

    public byte[] passwordBytes() { return passwordBytes; }
    public byte[] vaultKeyBytes() { return vaultKeyBytes; }
    public BigInteger a() { return a; }
    public BigInteger A() { return A; }
    public byte[] newSaltAuth() { return newSaltAuth; }
    public byte[] newVaultKeyIv() { return newVaultKeyIv; }
    public byte[] newSaltKey() { return newSaltKey; }
    public byte[] newPasswordBytes() { return newPasswordBytes; }
}
