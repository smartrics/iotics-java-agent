package smartrics.iotics.space.grpc;

import io.grpc.stub.StreamObserver;

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
