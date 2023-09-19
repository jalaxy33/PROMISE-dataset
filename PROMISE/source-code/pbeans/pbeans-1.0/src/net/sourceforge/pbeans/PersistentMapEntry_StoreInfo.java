package net.sourceforge.pbeans;

class PersistentMapEntry_StoreInfo extends AbstractStoreInfo {
    public PersistentMapEntry_StoreInfo() {
	super (PersistentMapEntry.class);
    }

    public Index[] getIndexes() {
	return new Index[] {
	    new Index(true, "name"),
	    new Index(false, "persistentMap")
	};
    }
}
