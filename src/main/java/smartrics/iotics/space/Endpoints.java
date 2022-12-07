package smartrics.iotics.space;

public record Endpoints(String resolver, String stomp, String qapi,
                        String grpc, String grpcWeb, String version) {
}
