package net.sourceforge.pbeans;

import net.sourceforge.pbeans.annotations.*;

@PersistentClass(
	indexes={
		@PropertyIndex(unique=true, keyLength=32, propertyNames={"globalObjectID","name","persistentMap"} )
	}
)
class PersistentMapEntry<TValue>  {
	private String iName;
	private GlobalPersistentID iGlobalObjectID;
	private PersistentMap iPersistentMap;

	@PersistentProperty(nullable=false)
	public String getName() {
		return this.iName;
	}

	public void setName (String name) {
		this.iName = name;
	}

	@PersistentProperty(nullable=false)
	public PersistentMap getPersistentMap() {
		return this.iPersistentMap;
	}

	public void setPersistentMap (PersistentMap map) {
		this.iPersistentMap = map;
	}

	public GlobalPersistentID getGlobalObjectID() {
		return iGlobalObjectID;
	}

	public void setGlobalObjectID(GlobalPersistentID globalObjectID) {
		iGlobalObjectID = globalObjectID;
	}
	
	public TValue getEntryValue(Store store) throws StoreException {
		GlobalPersistentID gid = this.iGlobalObjectID;
		return (TValue) (gid == null ? null : store.getObject(this.iGlobalObjectID));
	}
	
	public void setEntryValue(Store store, TValue value) {
		try {
			this.iGlobalObjectID = value == null ? null : store.getGlobalPersistentID(value);
		} catch(StoreException err) {
			throw new IllegalStateException(err);
		}
	}
}
