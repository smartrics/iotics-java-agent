package smartrics.iotics.space.grpc;

import com.iotics.api.FetchInterestResponse;
import com.iotics.api.SearchResponse;
import com.iotics.api.TwinID;

import java.util.Optional;

public record FeedData(TwinData twinData, SearchResponse.FeedDetails feedDetails, FetchInterestResponse fetchInterestResponse) {

    @Override
    public String toString() {
        return "DataDetails{" +
                "twinData=" + twinData +
                ", feed=" + feedDetails.getFeedId() +
                ", data=" + fetchInterestResponse.getPayload().getFeedData().getData().toStringUtf8() +
                '}';
    }
}
