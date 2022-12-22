package smartrics.iotics.space.twins;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.*;
import com.iotics.sdk.identity.Identity;
import com.iotics.sdk.identity.SimpleIdentityManager;
import smartrics.iotics.space.Builders;

import java.util.concurrent.Executor;

public class GenericModelTwin extends AbstractTwin {

    private final String label;
    private final String comment;
    private final String defines;
    private final String color;

    protected GenericModelTwin(SimpleIdentityManager sim,
                               String keyName,
                               TwinAPIGrpc.TwinAPIFutureStub stub,
                               Executor executor,
                               String label, String comment, String defines, String color) {
        super(sim, keyName, stub, executor);
        this.label = checkEmptyOrNull("label", label);
        this.comment = checkEmptyOrNull("comment", comment);
        this.color = checkEmptyOrNull("color", color);
        this.defines = checkEmptyOrNull("defines", defines);
    }

    public ListenableFuture<UpsertTwinResponse> make() {
        return getTwinAPIFutureStub().upsertTwin(UpsertTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did())
                        .build())
                .setPayload(UpsertTwinRequest.Payload.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS + "#comment")
                                .setLiteralValue(Literal.newBuilder().setValue(this.comment).build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS + "#label")
                                .setLiteralValue(Literal.newBuilder().setValue(this.label).build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDF + "#type")
                                .setUriValue(Uri.newBuilder().setValue("https://data.iotics.com/app#Model").build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey("https://data.iotics.com/app#defines")
                                .setUriValue(Uri.newBuilder().setValue(this.defines).build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey("https://data.iotics.com/app#color")
                                .setLiteralValue(Literal.newBuilder().setValue(this.color).build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey("http://data.iotics.com/public#hostAllowList")
                                .setUriValue(Uri.newBuilder().setValue("http://data.iotics.com/public#allHosts").build())
                                .build())
                        .build())
                .build());
    }

    private String checkEmptyOrNull(String p, String v) {
        if(Strings.isNullOrEmpty(v)) {
            throw new IllegalArgumentException("null " + p);
        }
        return v;
    }
}
