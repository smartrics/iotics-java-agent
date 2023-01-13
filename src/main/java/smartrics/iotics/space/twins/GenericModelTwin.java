package smartrics.iotics.space.twins;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.*;
import com.iotics.sdk.identity.SimpleIdentityManager;
import smartrics.iotics.space.Builders;

import java.util.concurrent.Executor;

import static smartrics.iotics.space.UriConstants.*;

public class GenericModelTwin extends AbstractTwin {

    private final String label;
    private final String comment;
    private final String defines;
    private final String color;

    protected GenericModelTwin(SimpleIdentityManager sim, String keyName, TwinAPIGrpc.TwinAPIFutureStub stub, Executor executor, String label, String comment, String defines, String color) {
        super(sim, keyName, stub, executor);
        this.label = checkEmptyOrNull("label", label);
        this.comment = checkEmptyOrNull("comment", comment);
        this.color = checkEmptyOrNull("color", color);
        this.defines = checkEmptyOrNull("defines", defines);
    }

    public ListenableFuture<UpsertTwinResponse> make() {
        return getTwinAPIFutureStub().upsertTwin(UpsertTwinRequest.newBuilder().setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build()).setPayload(UpsertTwinRequest.Payload.newBuilder().setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build()).addProperties(Property.newBuilder().setKey(ON_RDFS_COMMENT_PROP).setLiteralValue(Literal.newBuilder().setValue(this.comment).build()).build()).addProperties(Property.newBuilder().setKey(ON_RDFS_LABEL_PROP).setLiteralValue(Literal.newBuilder().setValue(this.label).build()).build()).addProperties(Property.newBuilder().setKey(ON_RDF_TYPE_PROP).setUriValue(Uri.newBuilder().setValue(IOTICS_APP_MODEL_VALUE).build()).build()).addProperties(Property.newBuilder().setKey(IOTICS_APP_DEFINES_PROP).setUriValue(Uri.newBuilder().setValue(this.defines).build()).build()).addProperties(Property.newBuilder().setKey(IOTICS_APP_COLOR_PROP).setLiteralValue(Literal.newBuilder().setValue(this.color).build()).build()).addProperties(Property.newBuilder().setKey(IOTICS_PUBLIC_ALLOW_LIST_PROP).setUriValue(Uri.newBuilder().setValue(IOTICS_PUBLIC_ALLOW_ALL_VALUE).build()).build()).build()).build());
    }

    private String checkEmptyOrNull(String p, String v) {
        if (Strings.isNullOrEmpty(v)) {
            throw new IllegalArgumentException("null " + p);
        }
        return v;
    }
}
