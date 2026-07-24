package xyz.segurapass.sdk;

import org.junit.jupiter.api.Test;
import xyz.segurapass.sdk.helpers.EncryptionHelper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class Argon2BenchmarkTest {

    @Test
    void benchmarkMasterPasswordKeyGeneration() {

        byte[] password = "correct horse battery staple".getBytes(StandardCharsets.UTF_8);

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        // Warmup JVM / JIT
        for (int i = 0; i < 5; i++) {
            EncryptionHelper.generateMasterPasswordKey(password, salt);
        }

        int runs = 20;
        long totalNanos = 0;

        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();

            SecretKey key = EncryptionHelper.generateMasterPasswordKey(
                        password,
                        salt
                );

            long end = System.nanoTime();

            totalNanos += (end - start);

            // Prevent optimization shenanigans
            if (key == null) {
                throw new AssertionError();
            }
        }

        double avgMs =
                totalNanos / (double) runs / 1_000_000.0;

        System.out.printf(
                "%nArgon2 average time: %.2f ms%n",
                avgMs
        );
    }
}