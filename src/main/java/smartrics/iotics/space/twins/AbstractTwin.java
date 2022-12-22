package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.iotics.api.*;
import com.iotics.sdk.identity.Identity;
import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public abstract class AbstractTwin implements Identifiable, Maker {

    public static final String ON_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
    public static final String ON_RDFS = "http://www.w3.org/2000/01/rdf-schema";

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
