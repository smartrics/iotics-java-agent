package smartrics.iotics.space.grpc;

import com.iotics.api.FetchInterestResponse;
import com.iotics.api.SearchResponse;
import com.iotics.api.TwinID;

import java.util.Optional;

public class DataDetails {
    private final TwinID modelTwinID;
    private final SearchResponse.FeedDetails feedDetails;
    private final SearchResponse.TwinDetails twinDetails;
    private final FetchInterestResponse fetchInterestResponse;

    public DataDetails(SearchResponse.TwinDetails twinDetails, SearchResponse.FeedDetails feedDetails, FetchInterestResponse fetchInterestResponse) {
        this(null, twinDetails, feedDetails, fetchInterestResponse);
    }

    public DataDetails(TwinID modelTwinID, SearchResponse.TwinDetails twinDetails, SearchResponse.FeedDetails feedDetails, FetchInterestResponse fetchInterestResponse) {
        this.modelTwinID = modelTwinID;
        this.feedDetails = feedDetails;
        this.twinDetails = twinDetails;
        this.fetchInterestResponse = fetchInterestResponse;
    }

    public Optional<TwinID> modelTwinID() {
        return Optional.ofNullable(this.modelTwinID);
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
                "model=" + modelTwinID +
                ", twin=" + twinDetails.getTwinId() +
                ", feed=" + feedDetails.getFeedId() +
                ", data=" + fetchInterestResponse.getPayload().getFeedData().getData().toStringUtf8() +
                '}';
    }
}
