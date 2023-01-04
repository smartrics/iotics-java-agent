package smartrics.iotics.space.twins;

import com.google.protobuf.BoolValue;
import com.iotics.api.*;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import smartrics.iotics.space.Builders;

import java.time.Duration;
import java.util.Iterator;

public interface Follower extends Identifiable {

    RetryPolicyBuilder<Object> DEF_RETRY_POLICY_FOLLOW = RetryPolicy.builder()
            .handle(StatusRuntimeException .class)
                                        .handleIf(e -> {
        StatusRuntimeException sre = (StatusRuntimeException) e;
        return sre.getStatus() == Status.DEADLINE_EXCEEDED
                || sre.getStatus() == Status.UNAUTHENTICATED
                || sre.getStatus() == Status.UNAVAILABLE;
    })
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .withJitter(Duration.ofMillis(100));

    InterestAPIGrpc.InterestAPIStub getInterestAPIStub();

    InterestAPIGrpc.InterestAPIBlockingStub getInterestAPIBlockingStub();

    default void followNoRetry(FeedID feedId, StreamObserver<FetchInterestResponse> responseStreamObserver)  {
        FetchInterestRequest request = newRequest(feedId);
        getInterestAPIStub().fetchInterests(request, responseStreamObserver);
    }

    default Iterator<FetchInterestResponse> follow(FeedID feedId)  {
        FetchInterestRequest request = newRequest(feedId);
        return getInterestAPIBlockingStub().fetchInterests(request);
    }

    default void follow(FeedID feedID, StreamObserver<FetchInterestResponse> responseStreamObserver) {
        follow(feedID, DEF_RETRY_POLICY_FOLLOW.build(), responseStreamObserver);
    }

    default void follow(FeedID feedID, RetryPolicy<Object> retryPolicy, StreamObserver<FetchInterestResponse> responseStreamObserver) {
        try {
            Failsafe.with(retryPolicy).run(() -> {
                Iterator<FetchInterestResponse> iterator = follow(feedID);
                while (iterator.hasNext()) {
                    FetchInterestResponse fetchInterestResponse = iterator.next();
                    responseStreamObserver.onNext(fetchInterestResponse);
                }
                responseStreamObserver.onCompleted();
            });
        } catch (FailsafeException t) {
            // when completed the retries
            responseStreamObserver.onError(t);
        } catch (Throwable t) {
            // for any non retryable exception
            responseStreamObserver.onError(t);
        }

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
