package smartrics.iotics.space;

import com.iotics.sdk.identity.IdentityManager;
import io.grpc.*;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public record TokenInjectorClientInterceptor(
        smartrics.iotics.space.TokenInjectorClientInterceptor.Scheduler scheduler) implements ClientInterceptor {

    interface Scheduler {
        void schedule();

        void cancel();

        String validToken();
    }

    public static class TimerScheduler implements Scheduler {

        private final Timer timer;
        private final IdentityManager identityManager;
        private final Duration duration;
        private AtomicReference<String> validToken;

        public TimerScheduler(IdentityManager identityManager, Timer timer, Duration duration) {
            this.timer = timer;
            this.identityManager = identityManager;
            this.validToken = new AtomicReference<>("");
            this.duration = duration;
        }

        @Override
        public void schedule() {
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // gets a token for thisI am gettin an user

                    // a token is used to auth this agent and user - the token has a validity. The longer the validity
                    // the lower the security - if token is stolen the thief can impersonate
                    validToken.set(identityManager.newAuthenticationToken(duration));
                }
            }, 0, duration.toMillis() - 100);

        }

        @Override
        public void cancel() {
            this.timer.cancel();
        }

        @Override
        public String validToken() {
            return validToken.get();
        }

    }

    public TokenInjectorClientInterceptor(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.scheduler.schedule();
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
