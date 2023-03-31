package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.ShareFeedDataRequest;
import smartrics.iotics.space.grpc.ListenableFutureAdapter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface MappablePublisher<T> extends Publisher, Mappable<T> {

    default CompletableFuture<Void> share() {
        List<ShareFeedDataRequest> list = getMapper().getShareFeedDataRequest(getTwinSource());

        List<CompletableFuture<?>> futures = list.stream()
                .map((Function<ShareFeedDataRequest, ListenableFuture<?>>) request ->
                        ioticsApi().feedAPIFutureStub().shareFeedData(request))
                .map((Function<ListenableFuture<?>, CompletableFuture<?>>) ListenableFutureAdapter::toCompletable)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }


}
