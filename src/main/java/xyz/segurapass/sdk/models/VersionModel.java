package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionModel {
    private String appVersion;
    private String protocolVersion;
    private boolean emailClientActive;
    private int credentialsLimit;
}
