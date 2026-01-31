package portablemama.app.framework;

import java.util.Map;

public class JSONUtils {

    /**
     * Safely get nested Map value
     */
    public static Object getNested(Map map, String... keys) {
        Map current = map;
        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (!(next instanceof Map)) return null;
            current = (Map) next;
        }
        return current.get(keys[keys.length - 1]);
    }
}
