package smartrics.iotics.space.identity;

import com.iotics.sdk.identity.IdentityManager;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class TimerScheduler implements TokenScheduler {

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
