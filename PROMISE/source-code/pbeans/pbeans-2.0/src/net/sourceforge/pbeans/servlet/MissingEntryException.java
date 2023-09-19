package net.sourceforge.pbeans.servlet;

public class MissingEntryException extends Exception {
    private final String keyName;
    private final String keyValue;

    public MissingEntryException(String keyName, String keyValue) {
        super("");
	this.keyName = keyName;
	this.keyValue = keyValue;
    }

    public String getIndexFieldName() {
	return this.keyName;
    }

    public String getIndexFieldValue() {
	return this.keyValue;
    }
}
