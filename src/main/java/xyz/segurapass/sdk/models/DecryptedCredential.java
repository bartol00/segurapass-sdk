package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecryptedCredential {
    private UUID credentialsId;
    private String website;
    private String username;
    private String password;
    private Instant createdAt;
    private Instant lastUpdated;
    private boolean passwordVisible;
}
