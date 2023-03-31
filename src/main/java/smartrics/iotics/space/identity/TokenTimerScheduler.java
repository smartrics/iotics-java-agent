package smartrics.iotics.space.identity;

import smartrics.iotics.identity.IdentityManager;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class TokenTimerScheduler implements TokenScheduler {

    private final Timer timer;
    private final IdentityManager identityManager;
    private final Duration duration;
    private final AtomicReference<String> validToken;

    TokenTimerScheduler(IdentityManager identityManager, Duration duration, Timer timer) {
        this.timer = timer;
        this.identityManager = identityManager;
        this.validToken = new AtomicReference<>();
        this.duration = duration;
    }

    @Override
    public void schedule() {
        if (timer == null) {
            return;
        }
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // a token is used to auth this agent and user - the token has a validity. The longer the validity
                // the lower the security - if token is stolen the thief can impersonate
                validToken.set(identityManager.newAuthenticationToken(duration));
            }
        }, 0, duration.toMillis() - 10);

    }

    @Override
    public void cancel() {
        if (timer != null) {
            this.timer.cancel();
        }
    }

    @Override
    public String validToken() {
        String value = validToken.get();
        if (value == null) {
            throw new IllegalStateException("not scheduled");
        }
        return value;
    }

}
