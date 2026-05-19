package com.segurapass.helpers;

import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public final class SrpLoginSession implements AutoCloseable {

    private byte[] passwordBytes;
    private BigInteger a;
    private BigInteger A;

    public static SrpLoginSession create(SecureRandom random, char[] password, SRP6GroupParameters group) {

        SrpLoginSession s = new SrpLoginSession();

        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        s.passwordBytes = new byte[bb.remaining()];
        bb.get(s.passwordBytes);

        s.a = new BigInteger(256, random);
        s.A = group.getG().modPow(s.a, group.getN());

        return s;
    }

    @Override
    public void close() {
        if (passwordBytes != null) Arrays.fill(passwordBytes, (byte) 0);
        passwordBytes = null;

        a = null;
        A = null;
    }

    public byte[] passwordBytes() { return passwordBytes; }
    public BigInteger a() { return a; }
    public BigInteger A() { return A; }
}
