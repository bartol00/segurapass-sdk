package com.segurapass.helpers;

import com.segurapass.exception.SdkException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;

public class SrpHelper {

    private static final SRP6GroupParameters group = SRP6StandardGroups.rfc5054_3072;
    
    public static BigInteger generateX(Digest digest, byte[] salt, byte[] identity, byte[] password) {
        return SRP6Util.calculateX(
                digest,
                group.getN(),
                salt,
                identity,
                password
        );
    }

    public static BigInteger generateVerifier(BigInteger x) {
        return group.getG().modPow(x, group.getN());
    }
    
    public static BigInteger generateS(Digest digest, BigInteger x, BigInteger a, BigInteger A, BigInteger B) {
        BigInteger u = SRP6Util.calculateU(digest, group.getN(), A, B);
        BigInteger k = SRP6Util.calculateK(digest, group.getN(), group.getG());
        return B.subtract(k.multiply(group.getG().modPow(x, group.getN())))
                .modPow(a.add(u.multiply(x)), group.getN());
    }
    
    public static BigInteger generateM1(Digest digest, BigInteger A, BigInteger B, BigInteger S) {
        return SRP6Util.calculateM1(digest, group.getN(), A, B, S);
    }
    
    public static BigInteger generateM2(Digest digest, BigInteger A, BigInteger B, BigInteger S, BigInteger M1) {
        return SRP6Util.calculateM2(digest, A, M1, S, B);
    }

    public static void validatePublicValue(BigInteger value, String name, String endpoint) {
        if (value.mod(group.getN()).equals(BigInteger.ZERO)) {
            throw new SdkException(
                    500,
                    "POST",
                    String.format("Invalid SRP verifier computed: %s", name),
                    endpoint
            );
        }
    }

    public static void validateVerifier(BigInteger verifier, String endpoint) {
        if (verifier.mod(group.getN()).equals(BigInteger.ZERO)) {
            throw new SdkException(
                    500,
                    "POST",
                    "Invalid SRP verifier computed",
                    endpoint
            );
        }
    }
    
}
