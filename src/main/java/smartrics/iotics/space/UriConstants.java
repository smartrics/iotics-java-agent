package smartrics.iotics.space;

public interface UriConstants {

    String IOTICS_APP_MODEL_PROP = "https://data.iotics.com/app#model";
    String IOTICS_APP_DEFINES_PROP = "https://data.iotics.com/app#defines";
    String IOTICS_APP_COLOR_PROP = "https://data.iotics.com/app#color";
    String IOTICS_PUBLIC_ALLOW_LIST_PROP = "http://data.iotics.com/public#hostAllowList";
    String IOTICS_PUBLIC_ALLOW_ALL_VALUE = "http://data.iotics.com/public#allHosts";
    String IOTICS_APP_MODEL_VALUE = "https://data.iotics.com/app#Model";

    String ON_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    String ON_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String ON_OWL = "http://www.w3.org/2002/07/owl#";

    String ON_RDF_TYPE_PROP = ON_RDF + "type";
    String ON_RDFS_LABEL_PROP = ON_RDFS + "label";
    String ON_RDFS_COMMENT_PROP = ON_RDFS + "comment";

    String ON_OWL_CLASS_VALUE = ON_OWL + "Class";
    String ON_RDFS_CLASS_VALUE = ON_RDFS + "Class";

    String IOTICS_CUSTOM_SEARCH_VALUE_PROP = "http://data.iotics.com/ont/searchQuery";
    String IOTICS_CUSTOM_SEARCH_TYPE_PROP = "http://data.iotics.com/ont/searchType";

}
