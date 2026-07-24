package xyz.segurapass.sdk.helpers;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.HKDFParameters;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class EncryptionHelper {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int ARGON2_ITERATIONS = 3;
    private static final int ARGON2_MEMORY_KB = 128 * 1024;
    private static final int ARGON2_PARALLELISM = 1;

    public static SecretKey generateMasterPasswordKey(byte[] password, byte[] salt) {
        byte[] keyBytes = new byte[32];

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withIterations(ARGON2_ITERATIONS)
                .withMemoryAsKB(ARGON2_MEMORY_KB)
                .withParallelism(ARGON2_PARALLELISM)
                .build();

        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(params);
        gen.generateBytes(password, keyBytes);

        SecretKey key = generateKeyFromBytes(keyBytes);
        Arrays.fill(keyBytes, (byte) 0);
        return key;
    }

    public static SecretKey deriveKeyHkdf(SecretKey key, byte[] salt, String info) {
        byte[] output = new byte[32];

        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        HKDFParameters params = new HKDFParameters(
                key.getEncoded(),
                salt,
                info.getBytes(StandardCharsets.UTF_8)
        );
        hkdf.init(params);
        hkdf.generateBytes(output, 0, output.length);

        SecretKey derivedKey = new SecretKeySpec(output, "AES");

        Arrays.fill(output, (byte) 0);

        return derivedKey;
    }

    public static KeyPair generateSigningKeypair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        return generator.generateKeyPair();
    }

    public static byte[] getPrivateSigningKeyBytes(PrivateKey privateKey) {
        return privateKey.getEncoded();
    }

    public static byte[] getPublicSigningKeyBytes(PublicKey publicKey) {
        return publicKey.getEncoded();
    }

    public static PrivateKey getPrivateSigningKeyFromBytes(byte[] privateKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("Ed25519");
        return factory.generatePrivate(
                new PKCS8EncodedKeySpec(privateKeyBytes)
        );
    }

    public static PublicKey getPublicSigningKeyFromBytes(byte[] publicKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("Ed25519");
        return factory.generatePublic(
                new X509EncodedKeySpec(publicKeyBytes)
        );
    }

    public static SecretKey generateKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(Arrays.copyOf(keyBytes, keyBytes.length), "AES");
    }

    public static byte[] encryptField(byte[] plaintext, byte[] iv, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        return cipher.doFinal(plaintext);
    }

    public static byte[] decryptField(byte[] cipherBytes, byte[] iv, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
        return cipher.doFinal(cipherBytes);
    }

}
