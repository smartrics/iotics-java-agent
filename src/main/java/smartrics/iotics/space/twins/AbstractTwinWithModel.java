package smartrics.iotics.space.twins;

import com.iotics.api.TwinID;
import smartrics.iotics.space.grpc.IoticsApi;

import java.util.concurrent.Executor;

public abstract class AbstractTwinWithModel extends AbstractTwin {

    private final TwinID modelDid;

    public AbstractTwinWithModel(IoticsApi api, String keyName,
                                 Executor executor,
                                 TwinID modelDid) {
        super(api, keyName, executor);
        this.modelDid = modelDid;
    }

    public TwinID getModelDid() {
        return this.modelDid;
    }
}
