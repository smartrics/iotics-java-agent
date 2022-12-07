package smartrics.iotics.space;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


public class IoticSpace {

    private final ServiceRegistry serviceRegistry;
    private Endpoints endpoints;

    public IoticSpace(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void initialise() throws IOException {
        this.endpoints = this.serviceRegistry.find();
    }

    @Override
    public String toString() {
        return "IoticSpace{" +
                "serviceRegistry=" + serviceRegistry +
                ", endpoints=" + endpoints +
                '}';
    }
}
