package smartrics.iotics.space.twins;

import com.google.rpc.Status;

public class SearchException  extends RuntimeException {
    private final Status status;

    public SearchException(String message, Status status) {
        super(message + ". Search status: [" + status + "]");
        this.status = status;
    }

}
