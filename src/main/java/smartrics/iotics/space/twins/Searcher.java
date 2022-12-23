package smartrics.iotics.space.twins;

import com.google.protobuf.StringValue;
import com.iotics.api.*;
import io.grpc.stub.StreamObserver;
import smartrics.iotics.space.Builders;

public interface Searcher extends Identifiable {

    SearchAPIGrpc.SearchAPIStub getSearchAPIStub();

    private static SearchRequest aSearchRequest(Headers headers, SearchFilter filter) {
        SearchRequest.Payload.Filter.Builder b = SearchRequest.Payload.Filter.newBuilder();
        filter.text().ifPresent(s -> b.setText(StringValue.newBuilder().setValue(s).build()));
        filter.geoLocation().ifPresent(s -> b.setLocation(s));
        filter.properties().forEach(p -> b.addProperties(p));
        SearchRequest.Payload.Filter f = b.build();
        SearchRequest.Builder rb = SearchRequest.newBuilder();
        SearchRequest.Payload.Builder pb = SearchRequest.Payload.newBuilder().setFilter(f);
        filter.expiryTimeout().ifPresent(t -> pb.setExpiryTimeout(t));
        filter.responseType().ifPresent(t -> pb.setResponseType(t));
        rb.setHeaders(headers).setPayload(pb.build());
        filter.scope().ifPresent(scope -> rb.setScope(scope));
        return rb.build();
    }

    default void search(SearchFilter filter, StreamObserver<SearchResponse.TwinDetails> twinDetailsStreamObserver) {

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
        SearchRequest request = aSearchRequest(Builders.newHeadersBuilder(getAgentIdentity().did()).build(), filter);
        getSearchAPIStub().synchronousSearch(request, obs);
    }
}
