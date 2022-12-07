package smartrics.iotics.space;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpServiceRegistry implements ServiceRegistry {
    private final String dns;
    private final OkHttpClient httpClient;

    public HttpServiceRegistry(String dns) {
        this.dns = dns;
        this.httpClient = new OkHttpClient();
    }

    @Override
    public Endpoints find() throws IOException {
        Request request = new Request.Builder()
                .url(String.format("https://%s/index.json", this.dns))
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            Gson gson = new Gson();
            return gson.fromJson(response.body().string(), Endpoints.class);
        }
    }

    @Override
    public String toString() {
        return "HttpServiceRegistry{" +
                "dns='" + dns + '\'' +
                '}';
    }
}
