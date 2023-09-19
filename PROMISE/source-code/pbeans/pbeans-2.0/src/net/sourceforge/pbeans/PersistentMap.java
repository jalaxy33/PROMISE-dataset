package net.sourceforge.pbeans;

import java.util.*;

import net.sourceforge.pbeans.annotations.*;

/**
 * A persistent map of strings to Persistent objects.
 * Note: Use this class only when you need a map of keys to objects
 * of different types. If you simply need to retrieve
 * objects of a known type by a property value, use one
 * of the {@link Store#selectParts(Object, Class, String)} methods in {@link Store}.
 */
@PersistentClass(
	indexes={
		@PropertyIndex(unique=true, propertyNames={"name"} )	
	}
)
public class PersistentMap<TKey,TValue> {
	private Store store;
	private String name;
	
	public PersistentMap() {
	}
	
	@TransientProperty
	public Store getStore() {
		return store;
	}

	/**
	 * This transient Store value must be
	 * set before map methods can work.
	 */
	public void setStore(Store store) {
		this.store = store;
	}

    @PersistentProperty(nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	private void assertStore() {
		if(this.store == null) {
			throw new IllegalStateException("Transient property store not set in PersistentMap");
		}
	}

	/**
	 * Maps a key to a value.
	 * @param key Must be of type String, Integer or Long.
	 * @param value Must implement Persistent.
	 */
	public TValue put(TKey key, TValue value) {
		this.assertStore();
		try {
			return this.putObject(key, value);		
		} catch (StoreException se) {
			throw new IllegalStateException (se.getMessage());
		}
	}

	
	/**
	 * Gets a persistent instance mapped to a string.
	 * @param key Must be of type String, Integer or Long.
	 */
	public TValue get(TKey key) {
		this.assertStore();
		try {
			return getObject (key);
		} catch (StoreException re) {
			throw new IllegalStateException (re.getMessage());
		}
	}

	/**
	 * Removes a mapping for the given key, if it exists.
	 * @param key Must be of type String, Integer or Long.
	 */
	public TValue remove(TKey key) {
		this.assertStore();
		try {
			return removeObject (key);
		} catch (StoreException re) {
			throw new IllegalStateException (re.getMessage());
		}    
	}

	public TValue putObject(TKey key, TValue value) throws StoreException {
		String name = getKeyString(key);
		PersistentMapEntry<TValue> entry;
		if ((entry = getEntry (name)) == null) {
			entry = new PersistentMapEntry<TValue>();
			entry.setName (name);
			entry.setEntryValue(store, value);
			entry.setPersistentMap (this);
			store.insert (entry);
			return null;
		}
		else {
			TValue prev = entry.getEntryValue(store);
			entry.setEntryValue(store, value);
			store.save (entry);
			return prev;
		}
	}

	private PersistentMapEntry<TValue> getEntry (String name) throws StoreException {
		this.assertStore();
		Map<String,Object> condValues = new HashMap<String,Object>();
		condValues.put ("name", name);
		condValues.put ("persistentMap", this);
		return (PersistentMapEntry<TValue>) store.selectSingle (PersistentMapEntry.class, condValues);
	}

	public TValue getObject(TKey key) throws StoreException {
		String name = getKeyString(key);
		PersistentMapEntry<TValue> me = getEntry(name);
		if (me == null) {
			return null;
		}
		return me.getEntryValue(store);
	}

	public TValue removeObject(TKey key) throws StoreException {
		String name = getKeyString(key);
		Map<String,Object> condValues = new HashMap<String,Object>();
		condValues.put ("name", name);
		condValues.put ("persistentMap", this);
		TValue prev = getObject(key);
		store.delete (PersistentMapEntry.class, condValues);
		return prev;
	}

	private String getKeyString(Object key) throws StoreException {
		// Note: Should not change this method in the future.
		if (key instanceof String) {
			return "S:" + key;
		}
		else if (key instanceof Integer) {
			return "I:" + key;
		}
		else if (key instanceof Long) {
			return "L:" + key;
		}
		else if (key instanceof Double) {
			return "D:" + key;
		}
		else if (key instanceof Byte) {
			return "B:" + key;
		}
		else if (key instanceof Short) {
			return "H:" + key;
		}
		else if (key instanceof Date) {
			return "A:" + ((Date) key).getTime();
		}
		else if (key instanceof GlobalPersistentID) {
			return "G:" + key;
		}
		else if (key == null) {
			throw new IllegalArgumentException ("key is null");
		}
		else {
			Class clazz = key.getClass();
			if(clazz.isAnnotationPresent(PersistentClass.class) || Persistent.class.isAssignableFrom(clazz)) {
				GlobalPersistentID gid = store.getGlobalPersistentID(key);
				return "P:" + gid;
			}
			else {
				throw new StoreException ("Non-persistent keys of type " + key.getClass().getName() + " are not supported by PersistentMap");
			}
		}
	}
}
