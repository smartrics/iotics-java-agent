package smartrics.iotics.space.twins;

import com.iotics.sdk.identity.Identity;

public interface Identifiable {
    Identity getIdentity();

    Identity getAgentIdentity();
}
