package smartrics.iotics.space.twins;

import com.google.rpc.Status;
import com.iotics.api.*;
import io.grpc.stub.StreamObserver;
import smartrics.iotics.space.Builders;

public interface Searcher extends Identifiable {

    SearchAPIGrpc.SearchAPIStub getSearchAPIStub();

    default void search(SearchRequest.Payload searchRequestPayload, StreamObserver<SearchResponse.TwinDetails> twinDetailsStreamObserver) {

        StreamObserver<SearchResponse> obs = new StreamObserver<>() {
            @Override
            public void onNext(SearchResponse searchResponse) {
                if(searchResponse.getPayload().hasStatus()) {
                    // error in the search response
                    twinDetailsStreamObserver.onError(new SearchException("search operation failure", searchResponse.getPayload().getStatus()));
                    return;
                }
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
        try {
            getSearchAPIStub().synchronousSearch(request, obs);
        } catch (Exception e) {
            e.printStackTrace();
            twinDetailsStreamObserver.onError(e);
            twinDetailsStreamObserver.onCompleted();
        }
    }
}
