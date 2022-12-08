package smartrics.iotics.space.grpc;

import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.ManagedChannelBuilder;
import smartrics.iotics.space.identity.TokenScheduler;
import smartrics.iotics.space.identity.TokenTimerScheduler;

import java.time.Duration;

public class HostManagedChannelBuilderFactory {

    private Duration tokenDuration;
    private int maxRetryAttempts = 0;
    private SimpleIdentityManager sim;
    private String grpcEndpoint;

    public HostManagedChannelBuilderFactory withSGrpcEndpoint(String endpoint) {
        this.grpcEndpoint = endpoint;
        return this;
    }

    public HostManagedChannelBuilderFactory withSimpleIdentityManager(SimpleIdentityManager sim) {
        this.sim = sim;
        return this;
    }

    public HostManagedChannelBuilderFactory withTokenTokenDuration(Duration tokenDuration) {
        this.tokenDuration = tokenDuration;
        return this;
    }

    public HostManagedChannelBuilderFactory withMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
        return this;
    }

    public ManagedChannelBuilder makeManagedChannelBuilder() {
        ManagedChannelBuilder builder = ManagedChannelBuilder.forTarget(grpcEndpoint);

        TokenScheduler scheduler = new TokenTimerScheduler(sim, Duration.ofSeconds(10));
        scheduler.schedule();
        TokenInjectorClientInterceptor interceptor = new TokenInjectorClientInterceptor(scheduler);
        builder.intercept(interceptor);

        builder.userAgent(sim.agentIdentity().did());

        if (this.maxRetryAttempts > 0) {
            builder.enableRetry().maxRetryAttempts(maxRetryAttempts);
        }
        return builder;
    }

}
