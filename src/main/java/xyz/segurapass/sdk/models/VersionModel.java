package xyz.segurapass.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionModel {
    private String versionNumber;
    private String versionDescription;
    private String downloadUrl;
    private LocalDate versionDate;
}
