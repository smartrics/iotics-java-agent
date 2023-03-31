package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.UpsertTwinRequest;
import com.iotics.api.UpsertTwinResponse;

public interface MappableMaker<T> extends Maker, Mappable<T> {

    default ListenableFuture<UpsertTwinResponse> make() {
        UpsertTwinRequest upsertRequest = getMapper().getUpsertTwinRequest(getTwinSource());
        return ioticsApi().twinAPIFutureStub().upsertTwin(upsertRequest);
    }

}
