package com.mipt.nikitabumagin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that exposes general application metadata such as name and version.
 *
 * <p>Property values are injected from the application configuration
 * ({@code app.name}, {@code app.version}) using Spring's
 * {@link org.springframework.beans.factory.annotation.Value @Value} mechanism, demonstrating
 * externalized configuration.</p>
 */
@Service
public class AppInfoService {

    private final String appName;
    private final String appVersion;

    public AppInfoService(
            @Value("${app.name}") String appName,
            @Value("${app.version}") String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }
}
