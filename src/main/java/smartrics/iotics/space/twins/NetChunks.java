package smartrics.iotics.space.twins;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.rpc.Status;
import com.iotics.api.SparqlQueryResponse;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class NetChunks {

    private final Multimap<String, SparqlQueryResponse> chunks;

    public NetChunks() {
        TreeMultimap<String, SparqlQueryResponse> m = TreeMultimap
                .create(Comparator.naturalOrder(), Comparator.comparingLong(t0 -> t0.getPayload().getSeqNum()));
        chunks = Multimaps.synchronizedSortedSetMultimap(m);
    }

    private static boolean responseCompleted(long receivedChunks, SparqlQueryResponse response) {
        boolean isLast = response.getPayload().getLast();
        boolean receivedAll = receivedChunks == response.getPayload().getSeqNum() + 1;
        return isLast && receivedAll;
    }

    public Result newChunk(SparqlQueryResponse response) {
        String hostId = Optional.of(response.getPayload().getHostId()).orElse("");
        Status status = response.getPayload().getStatus();
        chunks.put(hostId, response);
        return checkChunksCompleted(hostId);
    }

    public Boolean allCompleted() {
        return chunks.keys().stream()
                .map(this::checkChunksCompleted)
                .map(Result::completed)
                .reduce((completed1, completed2) -> completed1 && completed2).get();
    }

    private Result checkChunksCompleted(String hostId) {
        List<SparqlQueryResponse> hostResponses = Lists.newArrayList(chunks.get(hostId));
        SparqlQueryResponse lastResponse = hostResponses.get(hostResponses.size() - 1);
        if (!responseCompleted(hostResponses.size(), lastResponse)) {
            // incomplete
            return new Result(hostId, false, null);
        }
        String value = hostResponses.stream()
                .map(sparqlQueryResponse -> sparqlQueryResponse.getPayload().getResultChunk())
                .reduce((b1, b2) -> b1.concat(b2))
                .map(bytes -> bytes.toString(StandardCharsets.UTF_8))
                .get();
        return new Result(hostId, true, value);
    }

    public record Result(String hostId, boolean completed, String value) {

    }


}
