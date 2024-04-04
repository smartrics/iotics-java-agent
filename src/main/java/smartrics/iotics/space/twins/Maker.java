package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.*;
import com.iotics.api.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import smartrics.iotics.space.Builders;

import java.util.concurrent.Executor;

public interface Maker extends Identifiable, Describer {

    ListenableFuture<UpsertTwinResponse> make();

    default Executor getExecutor() {
        return MoreExecutors.directExecutor();
    }

    default ListenableFuture<DeleteTwinResponse> delete() {
        return ioticsApi().twinAPIFutureStub().deleteTwin(DeleteTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did())
                        .build())
                .setArgs(DeleteTwinRequest.Arguments.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build())
                        .build())
                .build());
    }

    default ListenableFuture<TwinID> makeIfAbsent() {
        SettableFuture<TwinID> fut = SettableFuture.create();
        Futures.addCallback(describe(), describeCallback(this, fut, getExecutor()), getExecutor());
        return fut;
    }


    private static FutureCallback<DescribeTwinResponse> describeCallback(Maker maker, SettableFuture<TwinID> future, Executor executor) {


        return new FutureCallback<>() {
            @Override
            public void onSuccess(DescribeTwinResponse describeTwinResponse) {
                future.set(describeTwinResponse.getPayload().getTwinId());
            }

            @Override
            public void onFailure(@NotNull Throwable thrown) {
                if ((thrown instanceof StatusRuntimeException sre) && sre.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    Futures.addCallback(maker.make(), makeCallback(future), executor);
                } else {
                    future.setException(thrown);
                }
            }
        };
    }

    @NotNull
    private static FutureCallback<UpsertTwinResponse> makeCallback(SettableFuture<TwinID> future) {
        return new FutureCallback<>() {
            @Override
            public void onSuccess(UpsertTwinResponse upsertTwinResponse) {
                future.set(upsertTwinResponse.getPayload().getTwinId());
            }

            @Override
            public void onFailure(@NotNull Throwable throwable) {
                future.setException(throwable);
            }
        };
    }


}
