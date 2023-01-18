package smartrics.iotics.space.twins;

import com.iotics.api.TwinAPIGrpc;
import com.iotics.api.TwinID;
import com.iotics.sdk.identity.SimpleIdentityManager;

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
