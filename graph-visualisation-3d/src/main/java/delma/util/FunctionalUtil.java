package delma.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author delma
 */
public enum FunctionalUtil {

    INSTANCE;

    public static <T, K> Optional<K> applyIfCan(Class<T> type, Object object, Function<T, K> code) {
        if (type.isInstance(object)) {
            return Optional.ofNullable(code.apply((T) object));
        }
        return Optional.empty();
    }

    public static <T> void acceptIfCan(Class<T> type, Object object, Consumer<T> code) {
        if (type.isInstance(object)) {
            code.accept((T) object);
        }
    }
}
