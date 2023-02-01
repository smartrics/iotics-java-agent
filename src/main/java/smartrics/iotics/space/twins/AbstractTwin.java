package smartrics.iotics.space.twins;

import com.iotics.sdk.identity.Identity;
import com.iotics.sdk.identity.SimpleIdentityManager;
import smartrics.iotics.space.grpc.IoticsApi;

import java.util.concurrent.Executor;

public abstract class AbstractTwin implements Identifiable, Maker {

    private final Identity identity;
    private final Executor executor;
    private final IoticsApi api;

    public AbstractTwin(IoticsApi api, String keyName,
                        Executor executor) {
        this.api = api;
        this.identity = this.api.getSim().newTwinIdentity(keyName);
        this.executor = executor;
    }

    @Override
    public Identity getIdentity() {
        return this.identity;
    }

    @Override
    public Identity getAgentIdentity() {
        return ((SimpleIdentityManager) ioticsApi().getSim()).agentIdentity();
    }

    @Override
    public IoticsApi ioticsApi() {
        return api;
    }

}
