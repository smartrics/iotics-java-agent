package smartrics.iotics.space.twins;

import com.iotics.api.*;
import io.grpc.stub.StreamObserver;
import smartrics.iotics.space.Builders;

public interface Searcher extends Identifiable {

    SearchAPIGrpc.SearchAPIStub getSearchAPIStub();

    default void search(SearchRequest.Payload searchRequestPayload, StreamObserver<SearchResponse.TwinDetails> twinDetailsStreamObserver) {

        StreamObserver<SearchResponse> obs = new StreamObserver<>() {
            @Override
            public void onNext(SearchResponse searchResponse) {
                for(SearchResponse.TwinDetails d: searchResponse.getPayload().getTwinsList()) {
                    twinDetailsStreamObserver.onNext(d);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                twinDetailsStreamObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                twinDetailsStreamObserver.onCompleted();
            }
        };
        SearchRequest request = SearchRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setPayload(searchRequestPayload)
                .build();
        getSearchAPIStub().synchronousSearch(request, obs);
    }
}
