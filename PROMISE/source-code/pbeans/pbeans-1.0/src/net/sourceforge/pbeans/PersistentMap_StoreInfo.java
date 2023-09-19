package net.sourceforge.pbeans;

class PersistentMap_StoreInfo extends AbstractStoreInfo {
    public PersistentMap_StoreInfo() {
	super (PersistentMap.class);
    }

    public Persistent create (Store store) {
	return new PersistentMap (store);
    }
}
