package net.sourceforge.pbeans;

/**
 * Special property type which may be used
 * instead of explicit persistent object references
 * in order to define relationships between persistent objects.
 * <p>
 * It allows referred objects to be loaded on demand.
 * @see Store#getObject(PersistentID,java.lang.Class)
 * @see Store#getPersistentID(Object)
 */
public final class PersistentID {
    private final long longID;

    PersistentID (long id) {
        this.longID = id;
    }

    public String toString() {
        return String.valueOf(this.longID);
    }

    public int hashCode() {
        return (int) this.longID;
    }

    public boolean equals (Object other) {
        return other instanceof PersistentID && ((PersistentID) other).longID == this.longID;
    }

    public static PersistentID valueOf(String s) {
        return new PersistentID (Long.parseLong(s));
    }

    Long longValue() {
        return new Long(this.longID);
    }
}
