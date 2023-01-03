package smartrics.iotics.space.twins;

import com.google.protobuf.BoolValue;
import com.iotics.api.*;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import smartrics.iotics.space.Builders;

import java.util.Iterator;

public interface Follower extends Identifiable {

    InterestAPIGrpc.InterestAPIStub getInterestAPIStub();

    InterestAPIGrpc.InterestAPIBlockingStub getInterestAPIBlockingStub();

    default void follow(FeedID feedId, StreamObserver<FetchInterestResponse> responseStreamObserver)  {
        FetchInterestRequest request = newRequest(feedId);
        getInterestAPIStub().fetchInterests(request, responseStreamObserver);
    }

    default Iterator<FetchInterestResponse> follow(FeedID feedId)  {
        FetchInterestRequest request = newRequest(feedId);
        return getInterestAPIBlockingStub().fetchInterests(request);
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
