package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientVersion {
    private String appVersion;
    private Integer protocolVersion;
    private String sha256;
    private Instant releaseDate;
}
