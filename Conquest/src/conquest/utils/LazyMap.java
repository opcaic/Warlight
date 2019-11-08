package conquest.utils;

import java.util.HashMap;

/**
 * Maps whose items are initialized on demand by {@link #create(Object)} method.
 * @author Jimmy
 */
public abstract class LazyMap<K, V> extends HashMap<K, V> {

    /**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = -8974300753033092991L;

	/**
     * Creates value for given key.
     * @param key
     * @return
     */
    protected abstract V create(K key);

    @Override
    public V get(Object key) {
        V val = super.get((K)key);
        if(val == null) {
        	synchronized(this) {
        		val = super.get((K)key);
        		if (val != null) return val;
	            val = create((K)key);
	            if(val != null) {
	                put((K)key, val);
	            }
        	}
        }
        return val;
    }

}
