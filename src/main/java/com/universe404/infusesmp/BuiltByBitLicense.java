package com.universe404.infusesmp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BuiltByBit V2 Embed API usage-rights check.
 * Use a shareable API token with ONLY resources.embed.latest scope.
 */
public final class BuiltByBitLicense {
    // BuiltByBit replaces this placeholder when processing a resource download.
    private static final String NONCE = "%%__NONCE__%%";
    private static final String ENDPOINT = "https://api.builtbybit.com/v2/resources/embed/latest?nonce=";
    private static final long CACHE_TTL_MILLIS = Duration.ofHours(24).toMillis();
    private final JavaPlugin plugin;

    public BuiltByBitLicense(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean verifyOnStartup() {
        File licenseFile = new File(plugin.getDataFolder(), "license.yml");
        YamlConfiguration license = YamlConfiguration.loadConfiguration(licenseFile);
        String token = license.getString("api-token", "").trim();
        if (token.isEmpty() || token.equalsIgnoreCase("PUT_SHAREABLE_TOKEN_HERE")) {
            plugin.getLogger().severe("BuiltByBit API token is not configured. Add a shareable token with ONLY resources.embed.latest scope to config.yml.");
            return false;
        }
        if (NONCE.equals("%%__NONCE__%%")) {
            plugin.getLogger().warning("BuiltByBit nonce placeholder is still present. Ensure this JAR is downloaded through your BuiltByBit resource listing.");
        }
        try {
            Boolean valid = requestUsageRights(token);
            if (valid != null) {
                writeCache(valid);
                if (!valid) plugin.getLogger().severe("This BuiltByBit account does not have usage rights for InfuseSMP-S3.");
                return valid;
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("BuiltByBit license verification failed: " + ex.getClass().getSimpleName() + ". Checking recent local verification cache.");
        }
        File cache = new File(plugin.getDataFolder(), "license-cache.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cache);
        long checked = yaml.getLong("checked-at", 0L);
        boolean cachedValid = yaml.getBoolean("usage-rights", false);
        if (cachedValid && checked > 0L && System.currentTimeMillis() - checked >= 0L
                && System.currentTimeMillis() - checked <= CACHE_TTL_MILLIS) {
            plugin.getLogger().warning("Using a recent cached BuiltByBit license verification because the API is unavailable.");
            return true;
        }
        plugin.getLogger().severe("No recent successful BuiltByBit verification is available; refusing to enable the paid plugin.");
        return false;
    }

    private Boolean requestUsageRights(String token) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT + java.net.URLEncoder.encode(NONCE, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Shared " + token)
                .header("Accept", "application/json")
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("BuiltByBit API returned HTTP " + response.statusCode());
        }
        Matcher matcher = Pattern.compile("\"has_usage_rights\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE)
                .matcher(response.body());
        if (!matcher.find()) throw new IOException("BuiltByBit response did not contain data.has_usage_rights");
        return Boolean.parseBoolean(matcher.group(1));
    }

    private void writeCache(boolean valid) {
        File cache = new File(plugin.getDataFolder(), "license-cache.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("usage-rights", valid);
        yaml.set("checked-at", System.currentTimeMillis());
        try {
            yaml.save(cache);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save license verification cache: " + ex.getMessage());
        }
    }
}
