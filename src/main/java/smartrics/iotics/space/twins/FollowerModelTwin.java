package smartrics.iotics.space.twins;

import smartrics.iotics.space.grpc.IoticsApi;

import java.util.concurrent.Executor;

public final class FollowerModelTwin extends GenericModelTwin {
    public FollowerModelTwin(IoticsApi api, Executor executor) {
        super(api, "follower_model_keyname", executor,
                "Follower Twin Model", "Follower MODEL",
                "https://data.iotics.com/ont/follower", "#40E0D0" /* turquoise */);
    }
}
