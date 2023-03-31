package smartrics.iotics.space.connector;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.iotics.api.Property;
import com.iotics.api.Uri;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PrefixGenerator {

    private static final String RES_NAME = "prefixes.cc.json";

    private final Map<String, String> prefixes = new HashMap<>();

    public PrefixGenerator() {
        InputStream res = PrefixGenerator.class.getClassLoader().getResourceAsStream(RES_NAME);
        if (res == null) {
            throw new IllegalArgumentException("prefixes file not found at " + RES_NAME);
        }
        Gson gson = new Gson();
        Map<?, ?> pf = gson.fromJson(new InputStreamReader(res), Map.class);
        pf.keySet().forEach((Consumer<Object>) o -> prefixes.put(o.toString(), pf.get(o).toString()));
    }

    public static final String DEF_PREFIX = "def";

    public String mapKeyToJsonKey(Property property) {
        return mapToPrefix(property.getKey());
    }

    public String mapValueToJsonKey(Property property) {
        if (!Strings.isNullOrEmpty(property.getUriValue().getValue())) {
            return removeInvalidJsonChars(property.getUriValue().getValue());
        }
        if (!Strings.isNullOrEmpty(property.getLangLiteralValue().getValue())) {
            return removeInvalidJsonChars(property.getLangLiteralValue().getValue());
        }
        if (!Strings.isNullOrEmpty(property.getLiteralValue().getValue())) {
            return removeInvalidJsonChars(property.getLiteralValue().getValue());
        }
        if (!Strings.isNullOrEmpty(property.getStringLiteralValue().getValue())) {
            return removeInvalidJsonChars(property.getStringLiteralValue().getValue());
        }
        return DEF_PREFIX;
    }

    public String mapToPrefix(Uri uriObject) {
        return mapToPrefix(uriObject.getValue());
    }

    @NotNull
    private String mapToPrefix(String propUri) {
        URI uri = URI.create(propUri);
        // remove query from uri
        try {
            URI uriWithoutQuery = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
            String fragment = uriWithoutQuery.getFragment();

            URI uriWithoutFragment = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
            String mapTo = uriWithoutFragment.toString();
            if (fragment != null) {
                mapTo = mapTo + "#";
            }
            String prefix = findPrefix(mapTo, hostnameToValidKey(uriWithoutQuery.getHost()));

            if (fragment != null) {
                return String.join("_", prefix, fragment);
            }
            URL urlWithoutQuery = uriWithoutQuery.toURL();
            String[] parts = urlWithoutQuery.getPath().split("/");
            return String.join("_", prefix, parts[parts.length - 1]);
        } catch (URISyntaxException | MalformedURLException e) {
            return DEF_PREFIX;
        }
    }

    private static String hostnameToValidKey(String hostname) {
        String[] parts = hostname.split("\\.");
        if (parts.length == 1) {
            return parts[0];
        }
        return String.join("_", parts[parts.length - 1], parts[parts.length - 2]);
    }


    private static String removeInvalidJsonChars(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (isValidJsonChar(c)) {
                output.append(c);
            }
        }
        return output.toString();
    }

    private static boolean isValidJsonChar(char c) {
        return (c == '_' || c == '$' || c == '-' || c == '+' || (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'));
    }


    private String findPrefix(String uriString, String def) {
        return prefixes.entrySet()
                .stream()
                .filter(entry -> uriString.contains(entry.getValue()))
                .map(e -> uriString.replace(e.getValue(), e.getKey()))
                .findFirst()
                .orElse(def);
    }
}
