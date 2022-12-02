package index.persist;

/**
 * A simple interface for persisting objects, PersistenceUtils provides
 * convenience methods for serializing and de-serializing these objects
 *
 * @author xcTorres
 * Created on 2019/05/08
 */
public interface Persistable {

    /**
     * Convert fields and data within an object to binary form for transmission
     * or storage.
     *
     * @return an array of bytes representing a binary stream representation of
     * the object.
     */
    byte[] toBinary();

    /**
     * Convert a stream of binary bytes to fields and data within an object.
     */
    void fromBinary(byte[] bytes);
}
