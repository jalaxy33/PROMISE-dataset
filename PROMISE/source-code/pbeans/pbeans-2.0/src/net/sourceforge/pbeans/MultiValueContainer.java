/*
 * Created 26.08.2005
 * $MultiValueContainer.java$
 * @author Phil
 *
 */
package net.sourceforge.pbeans;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author Phil
 * 26.08.2005
 */
public class MultiValueContainer implements IMultiValueContainer {

    //Collection / Map / Array / List?
    private Vector values;
    
    /**
     * 
     */
    public MultiValueContainer() {

        values = new Vector(10, 5);
    }
    

    /* (non-Javadoc)
     * @see net.sourceforge.pbeans.IMultiValueContainer#size()
     */
    public int size() {
        // TODO Auto-generated method stub
        return values.size();
    }

    
    /* (non-Javadoc)
     * @see net.sourceforge.pbeans.IMultiValueContainer#iterator()
     */
    public Iterator iterator() {
        // TODO Auto-generated method stub
        return values.iterator();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pbeans.IMultiValueContainer#add(java.lang.Object)
     */
    public boolean add(Object aValue) {
        
        return values.add(aValue);

    }

}
