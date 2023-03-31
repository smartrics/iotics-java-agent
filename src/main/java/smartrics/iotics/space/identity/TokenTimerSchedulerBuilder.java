package smartrics.iotics.space.identity;

import smartrics.iotics.identity.IdentityManager;

import java.time.Duration;
import java.util.Timer;

public final class TokenTimerSchedulerBuilder {
    private Timer timer = new Timer("token-timer-scheduler");
    private IdentityManager identityManager;
    private Duration duration;

    private TokenTimerSchedulerBuilder() {
    }

    public static TokenTimerSchedulerBuilder aTokenTimerScheduler() {
        return new TokenTimerSchedulerBuilder();
    }

    public TokenTimerSchedulerBuilder withTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public TokenTimerSchedulerBuilder withIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
        return this;
    }

    public TokenTimerSchedulerBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public TokenTimerScheduler build() {
        return new TokenTimerScheduler(identityManager, duration, timer);
    }
}
