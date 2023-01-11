package smartrics.iotics.space.grpc;

import com.iotics.api.SearchResponse;
import com.iotics.api.TwinID;
import smartrics.iotics.space.UriConstants;

import java.util.List;
import java.util.Optional;

public record TwinData(SearchResponse.TwinDetails twinDetails) {

    public Optional<TwinID> optionalModelTwinID() {
        return this.optionalModelTwinID(twinDetails.getTwinId().getHostId());
    }

    public Optional<TwinID> optionalModelTwinID(String hostID) {
        List<String> list = twinDetails.getPropertiesList().stream()
                .filter(property -> property.getKey().equals(UriConstants.IOTICS_APP_MODEL_PROP)).map(p -> p.getUriValue().getValue()).toList();
        if (list.size() > 0) {
            return Optional.of(TwinID.newBuilder().setId(list.get(0)).setHostId(hostID).build());
        }
        return Optional.ofNullable(null);
    }

}
