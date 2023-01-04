package smartrics.iotics.space.manual;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.iotics.api.Headers;
import com.iotics.sdk.identity.SimpleConfig;
import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bitcoinj.core.Base58;
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
import java.util.concurrent.ThreadLocalRandom;

public class Space {

    private final String dns;
    private final SimpleIdentityManager sim;
    private final IoticSpace ioticSpace;

    public Space(String dns) throws IOException {
        this.dns = dns;
        String base = "./src/test/resources/";
        SimpleConfig user = SimpleConfig.readConf(Paths.get(base,"test-user.json"));
        SimpleConfig agent = SimpleConfig.readConf(Paths.get(base,"test-agent.json"));

        HttpServiceRegistry sr = new HttpServiceRegistry(this.dns);

        ioticSpace = new IoticSpace(sr);
        ioticSpace.initialise();

        sim = SimpleIdentityManager.Builder
                .anIdentityManager()
                .withAgentKeyID("#test-agent-0")
                .withUserKeyID("#test-user-0")
                .withAgentKeyName(agent.keyName())
                .withUserKeyName(user.keyName())
                .withResolverAddress(ioticSpace.endpoints().resolver())
                .withUserSeed(user.seed())
                .withAgentSeed(agent.seed())
                .build();
    }

    public String agentDid() {
        return sim.agentIdentity().did();
    }

    public ManagedChannel hostManagedChannel() throws IOException {
        ManagedChannelBuilder channelBuilder = new HostManagedChannelBuilderFactory()
                .withSimpleIdentityManager(sim)
                .withSGrpcEndpoint(ioticSpace.endpoints().grpc())
                .withTokenTokenDuration(Duration.ofSeconds(10))
                .makeManagedChannelBuilder();
        return channelBuilder.keepAliveWithoutCalls(true).build();
    }

}
