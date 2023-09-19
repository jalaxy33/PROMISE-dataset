package net.sourceforge.pbeans.examples;

import net.sourceforge.pbeans.*;

public class RegressionBean implements Persistent {
    private float f;
    private short s;
    private byte[] ba;
    private char c;
    private java.util.Date date;

    public float getFloatValue() {
	return this.f;
    }

    public short getShortValue() {
	return this.s;
    }

    public byte[] getArrayValue() {
	return this.ba;
    }

    public char getCharValue() {
	return this.c;
    }

    public java.util.Date getDateValue() {
	return this.date;
    }

    public void setCharValue (char c) {
	this.c = c;
    }

    public void setFloatValue (float f) {
	this.f = f;
    }

    public void setShortValue (short s) {
	this.s = s;
    }

    public void setArrayValue (byte[] ba) {
	this.ba = ba;
    }

    public void setDateValue (java.util.Date d) {
	this.date = d;
    }
}
