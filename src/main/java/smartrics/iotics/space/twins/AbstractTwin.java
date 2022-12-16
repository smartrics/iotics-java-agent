package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.iotics.api.*;
import com.iotics.sdk.identity.Identity;
import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;

import java.util.concurrent.Executor;

public abstract class AbstractTwin {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTwin.class);

    public static final String ON_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
    public static final String ON_RDFS = "http://www.w3.org/2000/01/rdf-schema";

    protected final SimpleIdentityManager sim;
    protected final Identity identity;
    protected final Executor executor;
    protected final TwinAPIGrpc.TwinAPIFutureStub twinStub;

    public AbstractTwin(SimpleIdentityManager sim, String keyName,
                        TwinAPIGrpc.TwinAPIFutureStub twinStub,
                        Executor executor) {
        this.sim = sim;
        this.identity = this.sim.newTwinIdentity(keyName);
        this.executor = executor;
        this.twinStub = twinStub;
    }

    public abstract ListenableFuture<UpsertTwinResponse> make();

    public ListenableFuture<DescribeTwinResponse> describe() {
        return this.twinStub.describeTwin(DescribeTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(sim.agentIdentity().did())
                        .build())
                .setArgs(DescribeTwinRequest.Arguments.newBuilder()
                        .setTwinId(TwinID.newBuilder()
                                .setId(this.identity.did())
                                .build())
                        .build())
                .build());
    }

    public ListenableFuture<DeleteTwinResponse> delete() {
        return this.twinStub.deleteTwin(DeleteTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(sim.agentIdentity().did())
                        .build())
                .setArgs(DeleteTwinRequest.Arguments.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(this.identity.did()).build())
                        .build())
                .build());
    }

    public ListenableFuture<TwinID> makeIfAbsent() {
        SettableFuture<TwinID> fut = SettableFuture.create();
        Futures.addCallback(describe(), describe_callback(fut), executor);
        return fut;
    }

    @NotNull
    protected FutureCallback<DescribeTwinResponse> describe_callback(SettableFuture<TwinID> future) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(DescribeTwinResponse describeTwinResponse) {
                LOGGER.info("Twin exists already");
                future.set(describeTwinResponse.getPayload().getTwinId());
            }

            @Override
            public void onFailure(Throwable thrown) {
                if ((thrown instanceof StatusRuntimeException sre) && sre.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    Futures.addCallback(make(), make_callback(future), executor);
                } else {
                    future.setException(thrown);
                }
            }
        };
    }

    @NotNull
    protected FutureCallback<UpsertTwinResponse> make_callback(SettableFuture<TwinID> future) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(UpsertTwinResponse upsertTwinResponse) {
                LOGGER.info("Twin made");
                future.set(upsertTwinResponse.getPayload().getTwinId());
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOGGER.warn("Twin can't be made: " + throwable.getMessage());
                future.setException(throwable);
            }
        };
    }


}
