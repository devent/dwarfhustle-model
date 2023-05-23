package com.anrisoftware.dwarfhustle.model.api.objects;

/**
 * Returns {@link GameObject} game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface ObjectsGetter {

    /**
     * Exception if there was an error getting the {@link GameObject}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public static class ObjectsGetterException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ObjectsGetterException(String message, Throwable cause) {
            super(message, cause);
        }

        public ObjectsGetterException(String message) {
            super(message);
        }
    }

    /**
     * Returns the {@link GameObject}.
     */
    <T extends GameObject> T get(Class<T> typeClass, String type, Object key) throws ObjectsGetterException;

}
