package smartrics.iotics.space.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLoggingStreamObserver<T> implements StreamObserver<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoggingStreamObserver.class);
    private final String logEntity;

    public AbstractLoggingStreamObserver(String logEntity) {
        this.logEntity = logEntity;
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.info("Wrapped stream observer for {} error: {}", logEntity, throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        LOGGER.info("Wrapped stream observer for {} completed", logEntity);
    }
}
