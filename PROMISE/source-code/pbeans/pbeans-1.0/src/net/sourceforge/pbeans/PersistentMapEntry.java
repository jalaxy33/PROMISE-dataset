package net.sourceforge.pbeans;

class PersistentMapEntry implements Persistent {
    private String iName;
    private Persistent iValue;
    private PersistentMap iPersistentMap;

    public String getName() {
	return this.iName;
    }

    public void setName (String name) {
	this.iName = name;
    }

    public Persistent getPersistentValue() {
	return this.iValue;
    }

    public void setPersistentValue (Persistent value) {
	this.iValue = value;
    }

    public PersistentMap getPersistentMap() {
	return this.iPersistentMap;
    }

    public void setPersistentMap (PersistentMap map) {
	this.iPersistentMap = map;
    }
}
