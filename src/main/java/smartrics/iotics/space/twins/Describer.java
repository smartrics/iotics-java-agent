package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.iotics.api.DescribeTwinRequest;
import com.iotics.api.DescribeTwinResponse;
import com.iotics.api.TwinID;
import io.grpc.stub.StreamObserver;
import smartrics.iotics.space.Builders;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public interface Describer extends Identifiable, ApiUser {

    default void describe(TwinID twinID, Timer scheduler, Duration initialDelay, Duration pollingFrequency, StreamObserver<DescribeTwinResponse> result) {
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ListenableFuture<DescribeTwinResponse> f = describe(twinID);
                Futures.addCallback(f, new FutureCallback<>() {
                    @Override
                    public void onSuccess(DescribeTwinResponse describeTwinResponse) {
                        result.onNext(describeTwinResponse);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        result.onError(throwable);
                    }
                }, MoreExecutors.directExecutor());
            }
        }, initialDelay.toMillis(), pollingFrequency.toMillis());

    }

    default ListenableFuture<DescribeTwinResponse> describe() {
        return describe(TwinID.newBuilder().setId(getIdentity().did()).build());
    }

    default ListenableFuture<DescribeTwinResponse> describe(TwinID twinID) {
        return ioticsApi().twinAPIFutureStub().describeTwin(DescribeTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did())
                        .build())
                .setArgs(DescribeTwinRequest.Arguments.newBuilder()
                        .setTwinId(TwinID.newBuilder()
                                .setId(twinID.getId())
                                .setHostId(twinID.getHostId())
                                .build())
                        .build())
                .build());
    }
}
