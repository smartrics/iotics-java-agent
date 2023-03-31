package smartrics.iotics.space.twins;

import smartrics.iotics.identity.Identity;

public interface Identifiable {
    Identity getIdentity();

    Identity getAgentIdentity();
}
