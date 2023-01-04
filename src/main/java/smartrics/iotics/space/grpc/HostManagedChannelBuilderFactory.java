package smartrics.iotics.space.grpc;

import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannelBuilder;
import smartrics.iotics.space.identity.TokenScheduler;
import smartrics.iotics.space.identity.TokenTimerScheduler;
import smartrics.iotics.space.identity.TokenTimerSchedulerBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class HostManagedChannelBuilderFactory {

    private Duration tokenDuration;
    private SimpleIdentityManager sim;
    private String grpcEndpoint;
    private String userAgent;
    private Timer timer;

    public HostManagedChannelBuilderFactory withSGrpcEndpoint(String endpoint) {
        this.grpcEndpoint = endpoint;
        return this;
    }

    public HostManagedChannelBuilderFactory withTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public HostManagedChannelBuilderFactory withUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    public ManagedChannelBuilder makeManagedChannelBuilder() {
        ManagedChannelBuilder builder = ManagedChannelBuilder.forTarget(grpcEndpoint);

        TokenScheduler scheduler = TokenTimerSchedulerBuilder
                .aTokenTimerScheduler()
                .withTimer(timer)
                .withDuration(tokenDuration)
                .withIdentityManager(sim)
                .build();
        scheduler.schedule();

        TokenInjectorClientInterceptor tokenInjectorClientInterceptor = new TokenInjectorClientInterceptor(scheduler);
        List<ClientInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(tokenInjectorClientInterceptor);
        builder.intercept(interceptorList);
        if (userAgent == null) {
            builder.userAgent("UserAgent=" + sim.agentIdentity().did());
        } else {
            builder.userAgent(userAgent);
        }
        return builder;
    }

}
