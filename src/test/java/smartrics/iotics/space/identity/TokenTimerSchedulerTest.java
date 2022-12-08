package smartrics.iotics.space.identity;

import com.iotics.sdk.identity.IdentityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenTimerSchedulerTest {

    @Mock
    public IdentityManager identityManager;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void givenNotScheduledThrowsISE() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            newTTS(0).validToken();
        });
        assertEquals("not scheduled", thrown.getMessage());
    }

    @Test
    public void whenScheduledGeneratesValidTokenFromIdentityManager() {
        when(identityManager.newAuthenticationToken(any())).thenReturn("valid-token");
        TokenTimerScheduler tts = newTTS(100);
        try {
            tts.schedule();
            verify(identityManager).newAuthenticationToken(Duration.of(100, ChronoUnit.MILLIS));
            assertEquals("valid-token", tts.validToken());
        } finally {
            tts.cancel();
        }
    }

    private TokenTimerScheduler newTTS(long millisDuration) {
        return new TokenTimerScheduler(
                identityManager,
                Duration.of(millisDuration, ChronoUnit.MILLIS));

    }
}