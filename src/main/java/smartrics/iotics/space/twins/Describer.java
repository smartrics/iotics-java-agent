package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.DescribeTwinRequest;
import com.iotics.api.DescribeTwinResponse;
import com.iotics.api.TwinAPIGrpc;
import com.iotics.api.TwinID;
import smartrics.iotics.space.Builders;

public interface Describer extends Identifiable {

    TwinAPIGrpc.TwinAPIFutureStub getTwinAPIFutureStub();

    default ListenableFuture<DescribeTwinResponse> describe() {
        return getTwinAPIFutureStub().describeTwin(DescribeTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did())
                        .build())
                .setArgs(DescribeTwinRequest.Arguments.newBuilder()
                        .setTwinId(TwinID.newBuilder()
                                .setId(getIdentity().did())
                                .build())
                        .build())
                .build());
    }

}