package index.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class PersistableFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(PersistableFactory.class);
    private static PersistableFactory singletonInstance = null;
    private final Map<Class<Persistable>, Short> classRegistry;
    private final Map<Short, Supplier<Persistable>> constructorRegistry;

    private PersistableFactory() {
        classRegistry = new HashMap<>();
        constructorRegistry = new HashMap<>();
    }

    public static synchronized PersistableFactory getInstance() {
        if (singletonInstance == null) {
            final PersistableFactory internalFactory = new PersistableFactory();
            final Iterator<PersistableRegistrySpi> persistableRegistries = new SPIServiceRegistry(
                    PersistableFactory.class).load(PersistableRegistrySpi.class);
            while (persistableRegistries.hasNext()) {
                final PersistableRegistrySpi persistableRegistry = persistableRegistries.next();
                if (persistableRegistry != null) {
                    internalFactory.addRegistry(persistableRegistry);
                }
            }
            singletonInstance = internalFactory;
        }
        return singletonInstance;
    }

    protected void addRegistry(
            final PersistableRegistrySpi registry) {
        final PersistableRegistrySpi.PersistableIdAndConstructor[] persistables = registry.getSupportedPersistables();
        for (final PersistableRegistrySpi.PersistableIdAndConstructor p : persistables) {
            addPersistableType(
                    p.getPersistableId(),
                    p.getPersistableConstructor());
        }
    }

    protected void addPersistableType(
            final short persistableId,
            final Supplier<Persistable> constructor) {
        final Class persistableClass = constructor.get().getClass();
        if (classRegistry.containsKey(persistableClass)) {
            LOGGER.error("'" + persistableClass.getCanonicalName() + "' already registered with id '"
                    + classRegistry.get(persistableClass) + "'.  Cannot register '" + persistableClass + "' with id '"
                    + persistableId + "'");
            return;
        }
        if (constructorRegistry.containsKey(persistableId)) {
            String currentClass = "unknown";

            for (final Entry<Class<Persistable>, Short> e : classRegistry.entrySet()) {
                if (persistableId == e.getValue().shortValue()) {
                    currentClass = e.getKey().getCanonicalName();
                    break;
                }
            }
            LOGGER.error("'" + persistableId + "' already registered for class '" + (currentClass)
                    + "'.  Cannot register '" + persistableClass + "' with id '" + persistableId + "'");
            return;
        }
        classRegistry.put(
                persistableClass,
                persistableId);
        constructorRegistry.put(
                persistableId,
                constructor);
    }

    public Persistable newInstance(
            final short id) {
        final Supplier<Persistable> constructor = constructorRegistry.get(id);
        if (constructor != null) {
            return constructor.get();
        }
        return null;
    }

    public Map<Class<Persistable>, Short> getClassIdMapping() {
        return classRegistry;
    }

}
