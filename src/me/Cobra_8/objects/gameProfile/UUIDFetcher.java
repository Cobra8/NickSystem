package me.Cobra_8.objects.gameProfile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.util.UUIDTypeAdapter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author Cobra_8
 */
public class UUIDFetcher {

    private final String PROFILE_URL;

    private final Gson gson;

    private final HashMap<String, UUID> cache;

    public UUIDFetcher() {
        this.PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";

        this.gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        this.cache = new HashMap<>();
    }

    public UUID getUniqueID(String name) throws IOException {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + name).openConnection();

        UUIDFetcher.FetcherResponse repsonse = gson.fromJson(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")), UUIDFetcher.FetcherResponse.class);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return repsonse.getId();
        } else {
            return null;
        }
    }

    private class FetcherResponse {

        private final String name;
        private final UUID id;
        private final boolean legacy;

        public FetcherResponse(String name, UUID id) {
            this.name = name;
            this.id = id;
            this.legacy = false;
        }

        public FetcherResponse(String name, UUID id, boolean legacy) {
            this.name = name;
            this.id = id;
            this.legacy = legacy;
        }

        public String getName() {
            return name;
        }

        public UUID getId() {
            return id;
        }

        public boolean isLegacy() {
            return legacy;
        }

    }

}
