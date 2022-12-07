package smartrics.iotics.space;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpServiceRegistry implements ServiceRegistry {
    private final String dns;
    private final OkHttpClient httpClient;
    private final String registryUrl;

    public HttpServiceRegistry(String dns) {
        this.dns = dns;
        this.httpClient = new OkHttpClient();
        this.registryUrl = String.format("https://%s/index.json", this.dns);
    }

    @Override
    public HostEndpoints find() throws IOException {
        Request request = new Request.Builder()
                .url(this.registryUrl)
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            Gson gson = new Gson();
            return gson.fromJson(response.body().string(), HostEndpoints.class);
        }
    }

    public String registryUrl() {
        return registryUrl;
    }

    @Override
    public String toString() {
        return "HttpServiceRegistry{" +
                "dns='" + dns + '\'' +
                "registryUrl='" + registryUrl + '\'' +
                '}';
    }
}
