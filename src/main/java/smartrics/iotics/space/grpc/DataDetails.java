package smartrics.iotics.space.grpc;

import com.iotics.api.FetchInterestResponse;
import com.iotics.api.SearchResponse;

public class DataDetails {
    private SearchResponse.FeedDetails feedDetails;
    private SearchResponse.TwinDetails twinDetails;
    private FetchInterestResponse fetchInterestResponse;

    public DataDetails(SearchResponse.TwinDetails twinDetails, SearchResponse.FeedDetails feedDetails, FetchInterestResponse fetchInterestResponse) {
        this.feedDetails = feedDetails;
        this.twinDetails = twinDetails;
        this.fetchInterestResponse = fetchInterestResponse;
    }

    public SearchResponse.FeedDetails feedDetails() {
        return feedDetails;
    }

    public SearchResponse.TwinDetails twinDetails() {
        return twinDetails;
    }

    public FetchInterestResponse fetchInterestResponse() {
        return fetchInterestResponse;
    }

    @Override
    public String toString() {
        return "DataDetails{" +
                "twin=" + twinDetails.getTwinId() +
                ", feed=" + feedDetails.getFeedId() +
                ", data=" + fetchInterestResponse.getPayload().getFeedData().getData().toStringUtf8() +
                '}';
    }
}
