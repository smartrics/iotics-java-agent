package smartrics.iotics.space.grpc;

import com.iotics.api.FetchInterestResponse;
import com.iotics.api.SearchResponse;

public record FeedDataBag(TwinDataBag twinData, SearchResponse.FeedDetails feedDetails,
                          FetchInterestResponse fetchInterestResponse) {

    @Override
    public String toString() {
        return "DataDetails{" +
                "twinData=" + twinData +
                ", feed=" + feedDetails.getFeedId() +
                ", data=" + fetchInterestResponse.getPayload().getFeedData().getData().toStringUtf8() +
                '}';
    }
}
