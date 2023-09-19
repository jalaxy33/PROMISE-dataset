package net.sourceforge.pbeans;

/**
 * A globally unique identifier for persistent objects.
 */
public final class GlobalPersistentID {
	private final PersistentID objectID;
	private final String className;
	
	public GlobalPersistentID(final PersistentID objectID, final String className) {
		super();
		this.objectID = objectID;
		this.className = className;
	}
	
	public GlobalPersistentID(final PersistentID objectID, final Class clazz) {
		super();
		this.objectID = objectID;
		this.className = clazz.getName();
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof GlobalPersistentID)) {
			return false;
		}
		GlobalPersistentID gpid = (GlobalPersistentID) other;
		return gpid.objectID.equals(this.objectID) && gpid.className.equals(this.className);
	}
	
	public int hashCode() {
		return this.objectID.hashCode() ^ this.className.hashCode();
	}

	public String getClassName() {
		return className;
	}

	public PersistentID getObjectID() {
		return objectID;
	}
	
	public String toString() {
		return this.className + ":" + this.objectID;
	}
	
	public static GlobalPersistentID valueOf(String asText) {
		if(asText == null) {
			return null;
		}
		int idx = asText.indexOf(':');
		String className = asText.substring(0, idx);
		Long id = Long.valueOf(asText.substring(idx+1));
		return new GlobalPersistentID(new PersistentID(id), className);
	}
}
