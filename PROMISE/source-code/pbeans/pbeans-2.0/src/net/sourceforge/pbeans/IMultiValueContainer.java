/*
 * Created 26.08.2005
 * $IMultiValueContainer.java$
 * @author Phil
 *
 */
package net.sourceforge.pbeans;

import java.util.Iterator;

/**
 * @author Phil
 * 26.08.2005
 */
public interface IMultiValueContainer {
    //Map.Entry [] getValues();
    int size();
    Iterator iterator();
    boolean add(Object aValue); 
}
