package smartrics.iotics.space.manual;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.iotics.api.Headers;
import smartrics.iotics.identity.SimpleConfig;
import smartrics.iotics.identity.SimpleIdentityManager;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bitcoinj.core.Base58;
import smartrics.iotics.identity.jna.JnaSdkApiInitialiser;
import smartrics.iotics.identity.jna.SdkApi;
import smartrics.iotics.space.HttpServiceRegistry;
import smartrics.iotics.space.IoticSpace;
import smartrics.iotics.space.grpc.HostManagedChannelBuilderFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

public class Space {

    private final String dns;
    private final SimpleIdentityManager sim;
    private final IoticSpace ioticSpace;

    public Space(String dns) throws IOException {
        this.dns = dns;

        String seed = "956c0741e3d8cdfbb43bf00570578768867b2f2cb14f059d367cf9fbfc4b1bc2";

        SimpleConfig user = new SimpleConfig(seed, "demoUserKey", "#id-0987654");
        SimpleConfig agent = new SimpleConfig(seed, "demoAgentKey", "#id-1234567");

        HttpServiceRegistry sr = new HttpServiceRegistry(this.dns);

        ioticSpace = new IoticSpace(sr);
        ioticSpace.initialise();

        sim = SimpleIdentityManager.Builder
                .anIdentityManager()
                .withResolverAddress(ioticSpace.endpoints().resolver())
                .withAgentKeyID(agent.keyId())
                .withUserKeyID(user.keyId())
                .withAgentKeyName(agent.keyName())
                .withUserKeyName(user.keyName())
                .withUserSeed(user.seed())
                .withAgentSeed(agent.seed())
                .build();
    }

    public String agentDid() {
        return sim.agentIdentity().did();
    }

    public ManagedChannel hostManagedChannel() throws IOException {
        var channelBuilder = new HostManagedChannelBuilderFactory()
                .withSimpleIdentityManager(sim)
                .withTimer(new Timer())
                .withSGrpcEndpoint(ioticSpace.endpoints().grpc())
                .withTokenTokenDuration(Duration.ofSeconds(10))
                .makeManagedChannelBuilder();
        return channelBuilder.keepAliveWithoutCalls(true).build();
    }
}
