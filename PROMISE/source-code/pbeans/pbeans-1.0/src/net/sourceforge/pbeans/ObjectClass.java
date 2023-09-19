package net.sourceforge.pbeans;

class ObjectClass implements Persistent {
    private Long iObjectID;
    private String iClassName;

    public ObjectClass() {
    }

    public ObjectClass (Long objectID, String className) {
	this.iObjectID = objectID;
	this.iClassName = className;
    }

    public Long getObjectID() {
	return iObjectID;
    }

    public void setObjectID (Long p) {
	this.iObjectID = p;
    }

    public void setClassName (String cn) {
	this.iClassName = cn;
    }

    public String getClassName() {
	return this.iClassName;
    }
}

