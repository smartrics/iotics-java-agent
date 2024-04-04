package smartrics.iotics.space.twins;

import com.iotics.api.ShareFeedDataRequest;
import com.iotics.api.UpsertTwinRequest;
import smartrics.iotics.identity.Identity;

import java.util.List;

public interface Mapper {

    Identity getTwinIdentity();

    UpsertTwinRequest getUpsertTwinRequest();

    List<ShareFeedDataRequest> getShareFeedDataRequest();
}
