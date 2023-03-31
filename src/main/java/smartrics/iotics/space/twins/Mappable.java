package smartrics.iotics.space.twins;

public interface Mappable<T> {

    Mapper<T> getMapper();

    T getTwinSource();

}
