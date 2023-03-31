package smartrics.iotics.space.connector;

import smartrics.iotics.space.UriConstants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OntConstant {

    RDF_TYPE_URI(UriConstants.ON_RDF_TYPE_PROP),
    RDFS_CLASS_URI(UriConstants.ON_RDFS_CLASS_VALUE),
    OWL_CLASS_URI(UriConstants.ON_OWL_CLASS_VALUE);

    private static List<String> uris;
    private final String uri;

    OntConstant(String uri) {
        this.uri = uri;
    }

    public static List<String> uris() {
        if (uris == null) {
            OntConstant.uris = Arrays
                    .stream(OntConstant.values())
                    .map(ontConstants -> ontConstants.uri)
                    .collect(Collectors.toList());
        }
        return uris;
    }

}
