package net.sourceforge.pbeans.annotations;

import java.lang.annotation.*;

/**
 * Annotation that marks a class as persistent.
 * <p>
 * The following is an example of a class that uses
 * this annotation.
 * <p>
 * <code><pre>
 * &#64;PersistentClass(
 *   table="User",
 *   autoIncrement=true,
 *   idField="UserID",
 *   indexes=&#64;PropertyIndex(unique=true,propertyNames={"userName"})
 * }
 * public class User {
 *   public String getUserName() {
 *   ...
 *   }
 *   
 *   public void setUserName(String userName) {
 *   ...
 *   }
 * }
 * </pre></code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistentClass {
	/**
	 * The requested table name. If one is not provided, pBeans will
	 * map a unique name based on the fully-qualified class name.
	 */
	String table() default "";
	
	/**
	 * An array of property indexes that will be translated into
	 * database indexes. Indexes allow records to be looked up
	 * more quickly. Unique indexes ensure duplicate records are
	 * not allowed.
	 */
	PropertyIndex[] indexes() default {};
	
	/**
	 * When this value is true, the annotated table will not be automatically
	 * created or altered. The user is expected to keep the database
	 * up to date by other means. However, warnings will be logged
	 * if tables are not structured as expected.
	 */
	boolean userManaged() default false;
	
	/**
	 * When set to <code>true</code>, requests the ID field to be auto-increment. If auto-increment
	 * is not supported, or if this value is false, then random IDs will be
	 * used. 
	 */
	boolean autoIncrement() default false;

	/**
	 * In automatic schema evolution mode, this value indicates whether
	 * fields no longer mapped from bean properties should be removed from
	 * the underlying table. By default existing fields are not
	 * removed.
	 */
	boolean deleteFields() default false;
	
	/**
	 * Requests a name for the ID field. If this value is not provided,
	 * pBeans will use a special field name.
	 */
	String idField() default "JP__OBJECTID";
}