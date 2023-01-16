package smartrics.iotics.space.grpc;

import io.grpc.*;
import smartrics.iotics.space.identity.TokenScheduler;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public record TokenInjectorClientInterceptor(
        TokenScheduler scheduler) implements ClientInterceptor {

    public TokenInjectorClientInterceptor(TokenScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new HeaderAttachingClientCall(next.newCall(method, callOptions));
    }

    private final class HeaderAttachingClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {
        HeaderAttachingClientCall(ClientCall<ReqT, RespT> call) {
            super(call);
        }

        public void start(Listener<RespT> responseListener, Metadata headers) {
            // Store the token in the gRPC stub
            Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("authorization", ASCII_STRING_MARSHALLER);
            Metadata metadata = new Metadata();
            metadata.put(AUTHORIZATION_KEY, "bearer " + TokenInjectorClientInterceptor.this.scheduler.validToken());
            headers.merge(metadata);
            // System.out.println("Added " +  TokenInjectorClientInterceptor.this.validToken);
            super.start(responseListener, headers);
        }
    }
}
