package index.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.spi.ServiceRegistry;
import java.util.*;

/**
 * Compensate for VFSClassloader's failure to discovery SPI registered classes
 * (used by JBOSS and Accumulo).
 * <p>
 * To Use:
 * (1) Register class loaders:
 * (2) Look up SPI providers:
 * <p>
 * final Iterator<FieldSerializationProviderSpi> serializationProviders = new
 * SPIServiceRegistry(FieldSerializationProviderSpi.class).load(
 * FieldSerializationProviderSpi.class);
 */
public class SPIServiceRegistry extends ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPIServiceRegistry.class);
    private static final Set<ClassLoader> ClassLoaders = Collections.synchronizedSet(new HashSet<ClassLoader>());
    private final Set<ClassLoader> localClassLoaders = Collections.synchronizedSet(new HashSet<ClassLoader>());

    @SuppressWarnings("unchecked")
    public SPIServiceRegistry(
            Class<?> category) {
        super(
                (Iterator) Arrays.asList(
                        category).iterator());
    }

    public SPIServiceRegistry(
            Iterator<Class<?>> categories) {
        super(
                categories);
    }

    public static void registerClassLoader(
            ClassLoader loader) {
        ClassLoaders.add(loader);
    }

    public void registerLocalClassLoader(
            ClassLoader loader) {
        localClassLoaders.add(loader);
    }

    public <T> Iterator<T> load(
            final Class<T> service) {

        final Set<ClassLoader> checkset = new HashSet<ClassLoader>();
        final Set<ClassLoader> clSet = getClassLoaders();
        final Iterator<ClassLoader> loaderIt = clSet.iterator();

        return new Iterator<T>() {

            Iterator<T> spiIT = null;

            @Override
            public boolean hasNext() {
                while ((spiIT == null || !spiIT.hasNext()) && (loaderIt.hasNext())) {
                    final ClassLoader l = loaderIt.next();
                    if (checkset.contains(l)) {
                        continue;
                    }
                    checkset.add(l);
                    spiIT = (Iterator<T>) ServiceRegistry.lookupProviders(
                            service,
                            l);
                }
                return spiIT != null && spiIT.hasNext();
            }

            @Override
            public T next() {
                return (T) spiIT.next();
            }

            @Override
            public void remove() {
            }

        };
    }

    /**
     * Returns all class loaders to be used for scanning plugins. The following
     * class loaders are always included in the search:
     * <p>
     * <ul>
     * <li>{@linkplain Class#getClassLoader This object class loader}</li>
     * <li>{@linkplain Thread#getContextClassLoader The thread context class
     * loader}</li>
     * <li>{@linkplain ClassLoader#getSystemClassLoader The system class loader}
     * </li>
     * </ul>
     * <p>
     * Both locally registered (this instance) and globally registered
     * classloaders are included it the search.
     * <p>
     * Redundancies and parent classloaders are removed where possible. Possible
     * error conditions include security exceptions. Security exceptions are not
     * logger UNLESS the set of searchable classloaders is empty.
     *
     * @return Classloaders to be used for scanning plugins.
     */
    public final Set<ClassLoader> getClassLoaders() {
        final List<String> exceptions = new LinkedList<String>();
        final Set<ClassLoader> loaders = new HashSet<ClassLoader>();

        try {
            loaders.add(SPIServiceRegistry.class.getClassLoader());
        } catch (SecurityException ex) {
            LOGGER.warn(
                    "Unable to get the class loader",
                    ex);
            exceptions.add("SPIServiceRegistry's class loader : " + ex.getLocalizedMessage());
        }
        try {
            loaders.add(ClassLoader.getSystemClassLoader());
        } catch (SecurityException ex) {
            LOGGER.warn(
                    "Unable to get the system class loader",
                    ex);
            exceptions.add("System class loader : " + ex.getLocalizedMessage());
        }
        try {
            loaders.add(Thread.currentThread().getContextClassLoader());
        } catch (SecurityException ex) {
            LOGGER.warn(
                    "Unable to get the context class loader",
                    ex);
            exceptions.add("Thread's class loader : " + ex.getLocalizedMessage());
        }

        loaders.addAll(ClassLoaders);
        loaders.addAll(localClassLoaders);

        /**
         * Remove those loaders that are parents to other loaders.
         */
        final ClassLoader[] loaderSet = loaders.toArray(new ClassLoader[loaders.size()]);
        for (int i = 0; i < loaderSet.length; i++) {
            ClassLoader parent = loaderSet[i].getParent();
            try {
                while (parent != null) {
                    loaders.remove(parent);
                    parent = parent.getParent();
                }
            } catch (SecurityException ex) {
                LOGGER.warn(
                        "Unable to get the class loader",
                        ex);
                exceptions.add(loaderSet[i].toString() + "'s parent class loader : " + ex.getLocalizedMessage());
            }
        }
        if (loaders.isEmpty()) {
            LOGGER.warn("No class loaders available. Check security exceptions (logged next).");
            for (String exString : exceptions) {
                LOGGER.warn(exString);
            }
        }
        return loaders;
    }
}
