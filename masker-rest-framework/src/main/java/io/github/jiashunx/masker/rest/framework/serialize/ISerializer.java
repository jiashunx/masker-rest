package io.github.jiashunx.masker.rest.framework.serialize;

/**
 * @author jiashunx
 */
public interface ISerializer {

    byte[] serialize(Object object);

    <T> T deserialize(Class<T> klass, byte[] bytes);

}
