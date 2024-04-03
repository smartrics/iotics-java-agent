package smartrics.iotics.space.manual;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.iotics.api.Headers;
import com.iotics.api.ListAllTwinsRequest;
import com.iotics.api.ListAllTwinsResponse;
import com.iotics.api.TwinAPIGrpc;
import io.grpc.ManagedChannel;
import smartrics.iotics.space.Builders;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ListTwins {
    public static void main(String[] args) throws Exception {
        DemoSpace ds = new DemoSpace();
        ManagedChannel channel = ds.hostManagedChannel();
        CountDownLatch completed = new CountDownLatch(1);
        try {
            TwinAPIGrpc.TwinAPIFutureStub twinAPIStub = TwinAPIGrpc.newFutureStub(channel);
            ListAllTwinsRequest listRequest = ListAllTwinsRequest.newBuilder()
                    .setHeaders(Builders.newHeadersBuilder(ds.agentDid()).build())
                    .build();
            ListenableFuture<ListAllTwinsResponse> future = twinAPIStub.listAllTwins(listRequest);
            future.addListener(() -> {
                try {
                    ListAllTwinsResponse result = future.get();
                    System.out.println(result);
                    completed.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, MoreExecutors.directExecutor());
        } catch (Exception e) {
            e.printStackTrace();
        }
        completed.await();
        System.out.println("finishing");
        channel.shutdownNow();
        channel.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("terminated");
    }

}
