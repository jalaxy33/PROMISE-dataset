package net.sourceforge.pbeans;

import java.util.*;

/**
 * A persistent map of strings to Persistent objects.
 * Note: Use this class only when you need a map of keys to objects
 * of different types. If you simply need to retrieve
 * objects of a known type by a property value, use one
 * of the select() methods in Store.
 */
public class PersistentMap implements Persistent {
    private final Store store;

    public PersistentMap (Store store) {
	this.store = store;
    }

    /**
     * Maps a key to a value.
     * @param key Must be of type String, Integer or Long.
     * @param value Must implement Persistent.
     */
    public Object put (Object key, Object value) {
	try {
	    return this.putObject (key, (Persistent) value);
	} catch (StoreException se) {
	    throw new IllegalStateException (se.getMessage());
	}
    }

    /**
     * Gets a persistent instance mapped to a string.
     * @param key Must be of type String, Integer or Long.
     */
    public Object get (Object key) {
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
    public Object remove (Object key) {
	try {
	    return removeObject (key);
	} catch (StoreException re) {
	    throw new IllegalStateException (re.getMessage());
	}    
    }

//     public int size() {
// 	Map condValues = new HashMap();
// 	condValues.put ("name", name);
// 	condValues.put ("persistentMap", this);
// 	return store.getCount (PersistentMapEntry.class, condValues);
//     }

    public Persistent putObject (Object key, Persistent value) throws StoreException {
	String name = getKeyString(key);
	try {
	    Object lock = store.requestLock (PersistentMapEntry.class, "name", name);
	    try {
		PersistentMapEntry entry;
		if ((entry = getEntry (name)) == null) {
		    entry = new PersistentMapEntry();
		    entry.setName (name);
		    entry.setPersistentValue (value);
		    entry.setPersistentMap (this);
		    store.insert (entry);
		    return null;
		}
		else {
		    Persistent prev = entry.getPersistentValue();
		    entry.setPersistentValue (value);
		    store.save (entry);
		    return prev;
		}
	    } finally {
		store.relinquishLock (lock);
	    }
	} catch (InterruptedException ie) {
	    throw new StoreException (ie);
	}
    }

    private PersistentMapEntry getEntry (String name) throws StoreException {
	Map condValues = new HashMap();
	condValues.put ("name", name);
	condValues.put ("persistentMap", this);
	return (PersistentMapEntry) store.selectSingle (PersistentMapEntry.class, condValues);
    }

    public Persistent getObject (Object key) throws StoreException {
	String name = getKeyString(key);
	PersistentMapEntry me = getEntry (name);
	if (me == null) {
	    return null;
	}
	return me.getPersistentValue();
    }

    public Persistent removeObject (Object key) throws StoreException {
	String name = getKeyString(key);
	try {
	    Map condValues = new HashMap();
	    condValues.put ("name", name);
	    condValues.put ("persistentMap", this);
	    Object lock = store.requestLock (PersistentMapEntry.class, "name", name);
	    try {
		Persistent prev = getObject (name);
	        store.delete (PersistentMapEntry.class, condValues);
		return prev;
	    }
	    finally {
		store.relinquishLock (lock);
	    }
	} catch (InterruptedException ie) {
	    throw new StoreException (ie);
	}
    }

    private String getKeyString (Object key) throws StoreException {
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
	else if (key == null) {
	    throw new IllegalArgumentException ("key is null");
	}
	else {
	    throw new StoreException ("Keys of type " + key.getClass().getName() + " are not supported by PersistentMap");
	}
    }
}
