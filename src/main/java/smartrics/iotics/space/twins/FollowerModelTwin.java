package smartrics.iotics.space.twins;

import com.iotics.api.TwinAPIGrpc;
import com.iotics.sdk.identity.SimpleIdentityManager;

import java.util.concurrent.Executor;

public final class FollowerModelTwin extends GenericModelTwin {
    public FollowerModelTwin(SimpleIdentityManager sim, TwinAPIGrpc.TwinAPIFutureStub stub, Executor executor) {
        super(sim, "follower_model_keyname", stub, executor,
                "Follower Twin Model", "Follower MODEL",
                "https://data.iotics.com/ont/follower", "#40E0D0" /* turquoise */);
    }
}
