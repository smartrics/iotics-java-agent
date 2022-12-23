package smartrics.iotics.elastic;

import com.google.protobuf.Timestamp;
import com.iotics.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchFilter {
    private String text;
    private List<Property> properties;
    private GeoCircle location;
    //arbitrary defaults
    private Scope scope;
    private ResponseType responseType;
    private Timestamp timestamp;

    public Optional<String> text() {
        return Optional.ofNullable(text);
    }

    public List<Property> properties() {
        return properties;
    }

    public Optional<GeoCircle> geoLocation() {
        return Optional.ofNullable(location);
    }

    public Optional<Scope> scope() {
        return Optional.ofNullable(scope);
    }

    public Optional<Timestamp> expiryTimeout() {
        return Optional.ofNullable(timestamp);
    }

    public Optional<ResponseType> responseType() {
        return Optional.ofNullable(responseType);
    }

    public static final class Builder {
        private String text;
        private List<Property> properties = new ArrayList<>();
        private GeoCircle location;
        private Scope scope = Scope.LOCAL;
        private ResponseType responseType = ResponseType.FULL;
        private Timestamp timestamp = Timestamp.newBuilder().setSeconds(5).build();

        private Builder() {
        }

        public static Builder aSearchFilter() {
            return new Builder();
        }

        public Builder withTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withResponseType(ResponseType responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public Builder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public Builder addProperty(Property p) {
            this.properties.add(p);
            return this;
        }

        public Builder withLocation(GeoCircle location) {
            this.location = location;
            return this;
        }

        public Builder withScope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public SearchFilter build() {
            SearchFilter searchFilter = new SearchFilter();
            searchFilter.properties = this.properties;
            searchFilter.text = this.text;
            searchFilter.location = this.location;
            searchFilter.responseType = this.responseType;
            searchFilter.timestamp = this.timestamp;
            searchFilter.scope = this.scope;
            return searchFilter;
        }
    }
}
