package smartrics.iotics.space.grpc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ListenableFutureAdapter<T> {

    private final CompletableFuture<T> completableFuture;

    public ListenableFutureAdapter(ListenableFuture<T> listenableFuture) {
        this(listenableFuture, MoreExecutors.directExecutor());
    }

    public ListenableFutureAdapter(ListenableFuture<T> listenableFuture, Executor executor) {
        this.completableFuture = new CompletableFuture<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(listenableFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(T result) {
                completableFuture.complete(result);
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        }, executor);
    }

    public CompletableFuture<T> getCompletableFuture() {
        return completableFuture;
    }

    public static <T> CompletableFuture<T> toCompletable(ListenableFuture<T> listenableFuture) {
        ListenableFutureAdapter<T> listenableFutureAdapter = new ListenableFutureAdapter<>(listenableFuture);
        return listenableFutureAdapter.getCompletableFuture();
    }

}