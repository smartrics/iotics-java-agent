package smartrics.iotics.space;

public record HostEndpointVersion(
        String space,
        String host,
        String portal,
        String interactionEngine,
        String collectionEngine
) {
}
