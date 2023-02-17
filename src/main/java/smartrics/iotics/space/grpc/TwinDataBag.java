package smartrics.iotics.space.grpc;

import com.iotics.api.*;
import smartrics.iotics.space.UriConstants;

import java.util.List;
import java.util.Optional;

public record TwinDataBag(List<Property> properties, TwinID twinID, int feedsCount, int inputsCount,
                          GeoLocation location) {

    public static TwinDataBag from(SearchResponse.TwinDetails twinDetails) {
        return new TwinDataBag(
                twinDetails.getPropertiesList(),
                twinDetails.getTwinId(),
                twinDetails.getFeedsCount(),
                twinDetails.getInputsCount(),
                twinDetails.getLocation());
    }

    public static TwinDataBag from(DescribeTwinResponse twinDescription) {
        return new TwinDataBag(
                twinDescription.getPayload().getResult().getPropertiesList(),
                twinDescription.getPayload().getTwinId(),
                twinDescription.getPayload().getResult().getFeedsCount(),
                twinDescription.getPayload().getResult().getInputsCount(),
                twinDescription.getPayload().getResult().getLocation());
    }

    public Optional<TwinID> optionalModelTwinID() {
        return this.optionalModelTwinID(twinID.getHostId());
    }

    public Optional<TwinID> optionalModelTwinID(String hostID) {
        List<String> list = properties.stream()
                .filter(property -> property.getKey().equals(UriConstants.IOTICS_APP_MODEL_PROP)).map(p -> p.getUriValue().getValue()).toList();
        if (list.size() > 0) {
            return Optional.of(TwinID.newBuilder().setId(list.get(0)).setHostId(hostID).build());
        }
        return Optional.ofNullable(null);
    }

}
