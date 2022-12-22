package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.iotics.api.*;
import com.iotics.sdk.identity.SimpleIdentityManager;
import smartrics.iotics.space.Builders;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public abstract class AbstractTwinWithModel extends AbstractTwin {

    private final TwinID modelDid;

    public AbstractTwinWithModel(SimpleIdentityManager sim, String keyName,
                                 TwinAPIGrpc.TwinAPIFutureStub twinStub,
                                 Executor executor,
                                 TwinID modelDid) {
        super(sim, keyName, twinStub, executor);
        this.modelDid = modelDid;
    }

    public TwinID getModelDid() {
        return this.modelDid;
    }
}
