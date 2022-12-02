package index.persist;

import java.util.function.Supplier;

public interface PersistableRegistrySpi {
    public PersistableIdAndConstructor[] getSupportedPersistables();

    public static class PersistableIdAndConstructor {
        private final short persistableId;
        private final Supplier<Persistable> persistableConstructor;

        public PersistableIdAndConstructor(
                final short persistableId,
                final Supplier<Persistable> persistableConstructor) {
            this.persistableId = persistableId;
            this.persistableConstructor = persistableConstructor;
        }

        public short getPersistableId() {
            return persistableId;
        }

        public Supplier<Persistable> getPersistableConstructor() {
            return persistableConstructor;
        }
    }

}
