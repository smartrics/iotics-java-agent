package smartrics.iotics.space.twins;

import smartrics.iotics.identity.Identity;
import smartrics.iotics.identity.SimpleIdentityManager;
import smartrics.iotics.space.grpc.IoticsApi;

import java.util.concurrent.Executor;

public abstract class AbstractTwin implements Identifiable, Maker {

    private final Identity identity;
    private final Executor executor;
    private final IoticsApi api;

    public AbstractTwin(IoticsApi api, String keyName, Executor executor) {
        this.api = api;
        this.identity = this.api.getSim().newTwinIdentity(keyName, "#deleg-" + keyName.hashCode());
        this.executor = executor;
    }

    public AbstractTwin(IoticsApi api, Identity identity, Executor executor) {
        this.api = api;
        this.identity = identity;
        this.executor = executor;
    }

    @Override
    public Identity getIdentity() {
        return this.identity;
    }

    @Override
    public Identity getAgentIdentity() {
        return ioticsApi().getSim().agentIdentity();
    }

    @Override
    public IoticsApi ioticsApi() {
        return api;
    }

}
