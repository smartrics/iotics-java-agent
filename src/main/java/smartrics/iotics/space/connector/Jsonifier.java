package smartrics.iotics.space.connector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iotics.api.FetchInterestResponse;
import com.iotics.api.Property;
import com.iotics.api.SearchResponse;
import smartrics.iotics.space.grpc.FeedDatabag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Jsonifier {

    private final PrefixGenerator prefixGenerator;
    private final SimpleDateFormat df;


    public Jsonifier(PrefixGenerator prefixGenerator) {
        this.prefixGenerator = prefixGenerator;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);
    }

    public JsonObject toJson(FetchInterestResponse fir) {
        try {
            JsonObject val = JsonParser.parseString(fir.getPayload().getFeedData().getData().toStringUtf8()).getAsJsonObject();
            // useful for latency and those cases when timestamp isn't sent by pubisher
            val.addProperty("data_received_timestamp", df.format(new Date()));
            return val;
        } catch (Exception e) {
            JsonObject o = new JsonObject();
            o.addProperty("error", e.getMessage());
            o.addProperty("data", fir.getPayload().getFeedData().getData().toStringUtf8());
            return o;
        }
    }

    public JsonObject toJson(SearchResponse.TwinDetails twinDetails) {
        JsonObject o = new JsonObject();
        o.addProperty("id", twinDetails.getTwinId().getId());
        o.addProperty("hostId", twinDetails.getTwinId().getHostId());
        o.addProperty("feedsCount", twinDetails.getFeedsCount());
        o.addProperty("inputCount", twinDetails.getInputsCount());
        o.addProperty("propertiesCount", twinDetails.getPropertiesCount());
        if (twinDetails.hasLocation()) {
            JsonObject location = new JsonObject();
            location.addProperty("lat", twinDetails.getLocation().getLat());
            location.addProperty("lon", twinDetails.getLocation().getLon());
            o.add("location", location);
        }
        addProperties(o, twinDetails.getPropertiesList());
        return o;
    }

    public JsonObject toJson(SearchResponse.FeedDetails feedDetails) {
        JsonObject o = new JsonObject();
        o.addProperty("id", feedDetails.getFeedId().getId());
        o.addProperty("storeLast", feedDetails.getStoreLast());
        o.addProperty("propertiesCount", feedDetails.getPropertiesCount());
        List<Property> list = feedDetails.getPropertiesList();
        addProperties(o, list);
        return o;
    }

    public JsonObject toJson(FeedDatabag feedData) {
        JsonObject twin = toJson(feedData.twinData().twinDetails());
        JsonObject feed = toJson(feedData.feedDetails());
        JsonObject data = toJson(feedData.fetchInterestResponse());
        feed.add("values", data);
        String feedID = feedData.feedDetails().getFeedId().getId();
        twin.add(feedID, feed);
        return twin;
    }

    private void addProperties(JsonObject o, List<Property> list) {
        list.forEach(property -> {
            String jsonKey = prefixGenerator.mapKeyToJsonKey(property);
            o.addProperty(jsonKey, getValueOf(property));
        });
    }

    private static String getValueOf(Property property) {
        if (property.hasLangLiteralValue()) {
            return String.join("_", property.getLangLiteralValue().getValue(), property.getLangLiteralValue().getLang());
        }
        if (property.hasLiteralValue()) {
            return property.getLiteralValue().getValue();
        }
        if (property.hasStringLiteralValue()) {
            return property.getStringLiteralValue().getValue();
        }
        if (property.hasUriValue()) {
            return property.getUriValue().getValue();
        }
        return "";
    }
}
