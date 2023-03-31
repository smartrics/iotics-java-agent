package smartrics.iotics.space.twins;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.protobuf.ByteString;
import com.iotics.api.*;
import smartrics.iotics.identity.Identity;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;
import smartrics.iotics.space.grpc.IoticsApi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static smartrics.iotics.space.UriConstants.*;

public class JsonPathMapper implements Mapper<DocumentContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPathMapper.class);

    static {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new GsonJsonProvider();
            private final MappingProvider mappingProvider = new GsonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });

    }

    private final TwinConf twinMapperConf;
    private final IoticsApi api;

    public JsonPathMapper(IoticsApi api, TwinConf conf) {
        this.twinMapperConf = conf;
        this.api = api;
    }

    private static void runSilently(Runnable r) {
        try {
            r.run();
        } catch (Throwable e) {
            LOGGER.warn("exception when running runnable: {}", e.getMessage());
        }
    }

    @Override
    public List<ShareFeedDataRequest> getShareFeedDataRequest(DocumentContext jsonContext) {
        // TODO: Dup below
        Identity twinIdentity = getTwinIdentity(jsonContext);

        String twinDid = twinIdentity.did();
        List<ShareFeedDataRequest> list = new ArrayList<>();
        JsonObject toShare = new JsonObject();
        twinMapperConf.feeds().forEach(feedMapper -> {
            JsonObject values = jsonContext.read(feedMapper.path());
            values.keySet()
                    .stream()
                    .filter(s -> values.get(s).isJsonPrimitive())
                    .forEach(s -> toShare.add(s, values.get(s)));
            LOGGER.info("sharing data for {}: {}", twinDid, toShare);
            ShareFeedDataRequest shareFeedDataRequest = ShareFeedDataRequest.newBuilder()
                    .setHeaders(Builders.newHeadersBuilder(api.getSim().agentIdentity().did()))
                    .setArgs(ShareFeedDataRequest.Arguments.newBuilder()
                            .setFeedId(FeedID.newBuilder()
                                    .setId(feedMapper.name())
                                    .setTwinId(twinDid)
                                    .build())
                            .build())
                    .setPayload(ShareFeedDataRequest.Payload.newBuilder()
                            .setSample(FeedData.newBuilder()
                                    .setMime("application/json")
                                    .setData(ByteString.copyFrom(toShare.toString().getBytes(StandardCharsets.UTF_8)))
                                    .build())
                            .build())
                    .build();
            list.add(shareFeedDataRequest);
        });
        return list;
    }

    public Identity getTwinIdentity(DocumentContext jsonContext) {
        int twinId = twinMapperConf
                .idPaths()
                .stream()
                .map(s -> ((JsonPrimitive) jsonContext.read(s)).getAsString())
                .collect(Collectors.joining("|")).hashCode();
        Identity twinIdentity = api.getSim().newTwinIdentity("key_" + twinId);
        return twinIdentity;
    }

    @Override
    public UpsertTwinRequest getUpsertTwinRequest(DocumentContext jsonContext) {

        Identity id = getTwinIdentity(jsonContext);

        UpsertTwinRequest.Builder reqBuilder = UpsertTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(api.getSim().agentIdentity().did()));
        UpsertTwinRequest.Payload.Builder payloadBuilder = UpsertTwinRequest.Payload.newBuilder();
        payloadBuilder.setTwinId(TwinID.newBuilder().setId(id.did()));
        payloadBuilder.setVisibility(Visibility.PUBLIC);
        payloadBuilder.addProperties(Property.newBuilder()
                .setKey(IOTICS_PUBLIC_ALLOW_LIST_PROP)
                .setUriValue(Uri.newBuilder().setValue(IOTICS_PUBLIC_ALLOW_ALL_VALUE).build())
                .build());

        runSilently(() -> {
            JsonObject metadata = jsonContext.read(twinMapperConf.metadataPath());
            List<Property> props = metadata.keySet()
                    .stream()
                    .filter(s -> metadata.get(s).isJsonPrimitive())
                    .map(s -> Property.newBuilder()
                            .setKey(twinMapperConf.ontologyRoot() + s)
                            .setStringLiteralValue(
                                    StringLiteral.newBuilder()
                                            .setValue(metadata.get(s).getAsString())
                                            .build())
                            .build()).toList();
            props.forEach(payloadBuilder::addProperties);
        });

        runSilently(() -> {
            JsonObject location = jsonContext.read(twinMapperConf.locationPath());
            payloadBuilder.setLocation(GeoLocation.newBuilder()
                    .setLon(location.get("lon").getAsDouble())
                    .setLat(location.get("lat").getAsDouble())
                    .build());
        });

        runSilently(() -> {
            String twinLabel = twinMapperConf
                    .labelPaths()
                    .stream()
                    .map(s -> ((JsonPrimitive) jsonContext.read(s)).getAsString())
                    .collect(Collectors.joining(", "));
            payloadBuilder.addProperties(Property.newBuilder()
                    .setKey(ON_RDFS_LABEL_PROP)
                    .setLiteralValue(Literal.newBuilder().setValue(twinLabel).build())
                    .build());
            payloadBuilder.addProperties(Property.newBuilder()
                    .setKey(ON_RDFS_COMMENT_PROP)
                    .setLiteralValue(Literal.newBuilder().setValue(twinMapperConf.commentPrefix() + "'" + twinLabel + "'").build())
                    .build());
        });

        twinMapperConf.feeds().forEach(feedMapper -> {
            UpsertFeedWithMeta.Builder builder = UpsertFeedWithMeta.newBuilder()
                    .setId(feedMapper.name())
                    .setStoreLast(true)
                    .addProperties(Property.newBuilder()
                            .setKey(ON_RDFS_COMMENT_PROP)
                            .setLiteralValue(Literal.newBuilder().setValue("Comment for '" + feedMapper.name() + "'").build())
                            .build())
                    .addProperties(Property.newBuilder()
                            .setKey(ON_RDFS_LABEL_PROP)
                            .setLiteralValue(Literal.newBuilder().setValue(feedMapper.name()).build())
                            .build());
            runSilently(() -> {
                JsonObject values = jsonContext.read(feedMapper.path());
                values.keySet()
                        .stream()
                        .filter(s -> values.get(s).isJsonPrimitive())
                        .forEach(s -> builder.addValues(Value.newBuilder()
                                .setLabel(s)
                                .setDataType("string")
                                .setComment("Comment for '" + s + "'")
                                .build()));

            });
            payloadBuilder.addFeeds(builder.build());

        });

        reqBuilder.setPayload(payloadBuilder.build());

        return reqBuilder.build();

    }

    public record FeedConf(String name, String path) {
    }

    public record TwinConf(String ontologyRoot,
                    String keyPrefix,
                    List<String> idPaths,
                    List<String> labelPaths,
                    String locationPath,
                    String commentPrefix,
                    String metadataPath,
                    List<FeedConf> feeds) {
    }

}
