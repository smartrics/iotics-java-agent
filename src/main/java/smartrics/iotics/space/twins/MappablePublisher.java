package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.ShareFeedDataRequest;
import smartrics.iotics.space.grpc.ListenableFutureAdapter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface MappablePublisher extends Publisher, Mappable {

    default CompletableFuture<Void> share() {
        List<ShareFeedDataRequest> list = getMapper().getShareFeedDataRequest();

        Function<ShareFeedDataRequest, ListenableFuture<?>> function = request ->
                ioticsApi().feedAPIFutureStub().shareFeedData(request);

        return map(list, function);
    }

    private CompletableFuture<Void> map(List<ShareFeedDataRequest> list, Function<ShareFeedDataRequest, ListenableFuture<?>> function) {
        List<CompletableFuture<?>> futures = list.stream()
                .map(function)
                .map((Function<ListenableFuture<?>, CompletableFuture<?>>) ListenableFutureAdapter::toCompletable)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    }
}
