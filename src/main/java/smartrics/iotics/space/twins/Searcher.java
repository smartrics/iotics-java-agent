package smartrics.iotics.space.twins;

import com.iotics.api.SearchRequest;
import com.iotics.api.SearchResponse;
import com.iotics.api.SparqlQueryRequest;
import com.iotics.api.SparqlQueryResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;
import smartrics.iotics.space.grpc.AbstractLoggingStreamObserver;

public interface Searcher extends Identifiable, ApiUser {
    Logger LOGGER = LoggerFactory.getLogger(AbstractLoggingStreamObserver.class);

    default void query(SparqlQueryRequest.Payload payload, StreamObserver<String> result) {
        SparqlQueryRequest request = SparqlQueryRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setPayload(payload)
                .build();

        NetChunks chunks = new NetChunks();
        ioticsApi().metaAPIStub().sparqlQuery(request, new StreamObserver<>() {
            @Override
            public void onNext(SparqlQueryResponse response) {
                if (response.getPayload().hasStatus()) {
                    // error in the search response
                    result.onError(new SearchException("query operation failure", response.getPayload().getStatus()));
                    return;
                }
                NetChunks.Result c = chunks.newChunk(response);
                if (c.completed()) {
                    result.onNext(c.value());
                }
                LOGGER.info("ALL COMPLETED {}", chunks.allCompleted());
            }

            @Override
            public void onError(Throwable t) {
                result.onError(t);
            }

            @Override
            public void onCompleted() {
                result.onCompleted();
            }
        });
    }

    default void search(SearchRequest.Payload searchRequestPayload, StreamObserver<SearchResponse.TwinDetails> twinDetailsStreamObserver) {

        StreamObserver<SearchResponse> obs = new StreamObserver<>() {
            @Override
            public void onNext(SearchResponse searchResponse) {
                if (searchResponse.getPayload().hasStatus()) {
                    // error in the search response
                    twinDetailsStreamObserver.onError(new SearchException("search operation failure", searchResponse.getPayload().getStatus()));
                    return;
                }
                for (SearchResponse.TwinDetails d : searchResponse.getPayload().getTwinsList()) {
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
            ioticsApi().searchAPIStub().synchronousSearch(request, obs);
        } catch (Exception e) {
            e.printStackTrace();
            twinDetailsStreamObserver.onError(e);
            twinDetailsStreamObserver.onCompleted();
        }
    }
}
