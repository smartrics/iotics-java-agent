package smartrics.iotics.space.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopStreamObserver<T> implements StreamObserver<T> {
    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onNext(T value) {

    }

    @Override
    public void onCompleted() {

    }
}
