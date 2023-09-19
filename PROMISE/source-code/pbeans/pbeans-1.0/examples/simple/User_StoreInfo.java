package net.sourceforge.pbeans.examples;

import java.beans.PropertyDescriptor;
import java.sql.Types;
import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.data.*;

/**
 * Persistence customization for class User.
 */
public class User_StoreInfo extends AbstractStoreInfo {
    public User_StoreInfo() {
        super (User.class);
    }

    public FieldDescriptor getFieldDescriptor (Store store, PropertyDescriptor pd) {
        FieldDescriptor oldDesc = super.getFieldDescriptor (store, pd);
        if (oldDesc == null) {
            // Transient property
            return null;
        }
        else if ("userID".equals (oldDesc.getName())) {
            // Request that property userID be renamed from userName.
            return new FieldDescriptor (oldDesc, new String[] { "userName" });
        }
        else {
            return oldDesc;
        }
    }

    public Index[] getIndexes (Store store) {
        // userID is a unique index
        return new Index[] {
            new Index (true, "userID")
        };
    }
}
