package smartrics.iotics.space.grpc;

import io.grpc.stub.StreamObserver;

import java.util.function.Function;

public abstract class AbstractMappingStreamObserver<T, K> implements StreamObserver<T>, Function<T, K> {

    private final StreamObserver<K> delegate;

    public AbstractMappingStreamObserver(StreamObserver<K> delegate) {
        this.delegate = delegate;
    }

    public void onNext(T data) {
        delegate.onNext(apply(data));
    }

    @Override
    public void onError(Throwable throwable) {
        delegate.onError(throwable);
    }

    @Override
    public void onCompleted() {
        delegate.onCompleted();
    }

}
