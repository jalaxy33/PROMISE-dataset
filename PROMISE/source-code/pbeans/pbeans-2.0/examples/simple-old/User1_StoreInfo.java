import java.beans.PropertyDescriptor;
import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.data.*;

/**
 * Persistence customization for class User.
 */
public class User1_StoreInfo extends AbstractStoreInfo {
    public User1_StoreInfo() {
        super (User1.class);
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
