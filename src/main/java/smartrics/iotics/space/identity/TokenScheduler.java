package smartrics.iotics.space.identity;

public interface TokenScheduler {
    void schedule();

    void cancel();

    String validToken() throws IllegalStateException;
}
