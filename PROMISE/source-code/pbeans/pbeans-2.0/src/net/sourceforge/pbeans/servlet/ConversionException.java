package net.sourceforge.pbeans.servlet;

public class ConversionException extends Exception {
    private final Class propertyType;
    private final String propertyName;

    public ConversionException(Class propertyType, String propName, String message) {
        super(message);
	this.propertyType = propertyType;
	this.propertyName = propName;
    }

    public Class getPropertyType() {
	return this.propertyType;
    }

    public String getPropertyName() {
	return this.propertyName;
    }
}
