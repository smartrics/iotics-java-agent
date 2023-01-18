package smartrics.iotics.space.twins;

import com.google.protobuf.BoolValue;
import com.iotics.api.*;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import dev.failsafe.function.CheckedPredicate;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public interface Follower extends Identifiable {

    record RetryConf(Duration delay, Duration jitter, Duration backoffDelay, Duration backoffMaxDelay) {}
    RetryPolicyBuilder<Object> DEF_RETRY_POLICY_FOLLOW_BUILDER = RetryPolicy.builder()
            .handle(StatusRuntimeException .class)
                                        .handleIf(e -> {
        StatusRuntimeException sre = (StatusRuntimeException) e;
        return sre.getStatus() == Status.DEADLINE_EXCEEDED
                || sre.getStatus() == Status.UNAUTHENTICATED
                || sre.getStatus() == Status.UNAVAILABLE;
    })
            .withDelay(Duration.ofSeconds(10))
            .withMaxRetries(-1)
            .withJitter(Duration.ofMillis(3000));

    InterestAPIGrpc.InterestAPIStub getInterestAPIStub();

    InterestAPIGrpc.InterestAPIBlockingStub getInterestAPIBlockingStub();

    default Iterator<FetchInterestResponse> follow(FeedID feedId)  {
        FetchInterestRequest request = newRequest(feedId);
        return getInterestAPIBlockingStub().fetchInterests(request);
    }

    default void followNoRetry(FeedID feedId, StreamObserver<FetchInterestResponse> observer)  {
        FetchInterestRequest request = newRequest(feedId);
        getInterestAPIStub().fetchInterests(request, observer);
    }

    default void follow(FeedID feedID, RetryConf retryConf, StreamObserver<FetchInterestResponse> observer) {
        Failsafe.with(DEF_RETRY_POLICY_FOLLOW_BUILDER
                        .withJitter(retryConf.jitter)
                        .withDelay(retryConf.delay)
                .build()).run(() -> {
            CompletableFuture<Void> result = new CompletableFuture<>();
            followNoRetry(feedID, new StreamObserver<>(){

                @Override
                public void onNext(FetchInterestResponse value) {
                    observer.onNext(value);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    result.completeExceptionally(t);
                }

                @Override
                public void onCompleted() {
                    result.complete(null);
                }
            });
            try {
                result.get();
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }

    // this is needed to stop the follow when the application needs to stop
    default Context.CancellableContext getCancellableContext() {
        return Context.current().withCancellation();
    }

    @NotNull
    private FetchInterestRequest newRequest(FeedID feedId) {
        FetchInterestRequest request = FetchInterestRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setFetchLastStored(BoolValue.newBuilder().setValue(true).build())
                .setArgs(FetchInterestRequest.Arguments.newBuilder()
                        .setInterest(Interest.newBuilder()
                                .setFollowerTwinId(TwinID.newBuilder().setId(getIdentity().did()))
                                .setFollowedFeedId(feedId)
                                .build())
                        .build())
                .build();
        return request;
    }

}
