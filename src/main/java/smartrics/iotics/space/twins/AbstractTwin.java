package smartrics.iotics.space.twins;

import com.iotics.api.TwinAPIGrpc;
import com.iotics.sdk.identity.Identity;
import com.iotics.sdk.identity.SimpleIdentityManager;

import java.util.concurrent.Executor;

public abstract class AbstractTwin implements Identifiable, Maker {

    private final SimpleIdentityManager sim;
    private final Identity identity;
    private final Executor executor;

    private final TwinAPIGrpc.TwinAPIFutureStub twinStub;

    public AbstractTwin(SimpleIdentityManager sim, String keyName,
                        TwinAPIGrpc.TwinAPIFutureStub stub,
                        Executor executor) {
        this.sim = sim;
        this.identity = this.sim.newTwinIdentity(keyName);
        this.executor = executor;
        this.twinStub = stub;
    }

    @Override
    public TwinAPIGrpc.TwinAPIFutureStub getTwinAPIFutureStub() {
        return this.twinStub;
    }

    @Override
    public Identity getIdentity() {
        return this.identity;
    }

    @Override
    public Identity getAgentIdentity() {
        return getSim().agentIdentity();
    }

    protected SimpleIdentityManager getSim() {
        return this.sim;
    }

}
