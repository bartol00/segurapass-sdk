package xyz.segurapass.sdk;

import xyz.segurapass.sdk.service.*;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.service.impl.*;
import lombok.Setter;

import java.util.function.Supplier;

public class SegurapassClient {

    @Setter
    private String jwt;
    private final AuthorizationService authorizationService;
    private final CredentialsService credentialsService;
    private final DeletionService deletionService;
    private final KeyService keyService;
    private final PasswordChangeService passwordChangeService;
    private final UptimeService uptimeService;
    private final VersionService versionService;

    public SegurapassClient(ApiClient apiClient) {
        this.authorizationService = new AuthorizationServiceImpl(apiClient);
        this.credentialsService = new CredentialsServiceImpl(apiClient, jwtSupplier());
        this.deletionService = new DeletionServiceImpl(apiClient, jwtSupplier());
        this.keyService = new KeyServiceImpl(apiClient);
        this.passwordChangeService = new PasswordChangeServiceImpl(apiClient, jwtSupplier());
        this.uptimeService = new UptimeServiceImpl();
        this.versionService = new VersionServiceImpl(apiClient);
    }

    public Supplier<String> jwtSupplier() {
        return () -> jwt;
    }

    public AuthorizationService auth() {
        return authorizationService;
    }

    public CredentialsService credentials() {
        return credentialsService;
    }

    public DeletionService deletion() {
        return deletionService;
    }

    public KeyService keys()  {
        return keyService;
    }

    public PasswordChangeService passwordChange() {
        return passwordChangeService;
    }

    public UptimeService uptime() {
        return uptimeService;
    }

    public VersionService version() {
        return versionService;
    }

}
