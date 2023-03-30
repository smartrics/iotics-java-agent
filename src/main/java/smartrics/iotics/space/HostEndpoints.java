package smartrics.iotics.space;

public record HostEndpoints(
        String resolver,
        String stomp,
        String qapi,
        String grpc,
        String grpcWeb,
        HostEndpointVersion version) {
}

