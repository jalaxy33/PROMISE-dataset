package net.sourceforge.pbeans.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a persistent bean property. It can be put next
 * to a getter or setter, but not both. The TransientProperty annotation
 * takes precendence.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PersistentProperty {
	/**
	 * The requested field name.
	 */
	String field() default "";
	
	/**
	 * Whether the database field should be declared nullable.
	 */
	boolean nullable() default true;
	
//	/**
//	 * The SQL type (see Types class). This annotation value
//	 * should normally not be provided. 
//	 */
//	int sqlType() default 0;

	/**
	 * If the property is renamed from another, provide 
	 * the name of the renamed <i>field</i> with this annotation 
	 * value. (Unless explicitly requested with the <code>fieldName<code>
	 * value, or unless truncated due to length, field names are
	 * the same as property names).
	 * <p>
	 * Normally when a property is renamed the effect is no different
	 * to removing the old property and creating a new one, which
	 * will result in loss of all data stored by the renamed property.
	 * This annotation value helps retain the old property's data.
	 */
	String renamedFrom() default "";

	/**
	 * Define this element as <code>true</code> when a property
	 * must be able to refer to persistent objects of different
	 * types. A property whose compile-time type is <code>Object</code>
	 * may be defined to be a global reference by means of this
	 * element. Persistent reference properties not defined to
	 * be global must have a compile-time type that exactly matches the
	 * run-time type of property values. 
	 * <p>
	 * Use of this element is not recommended under normal
	 * circumstances.
	 */
	boolean globalReference() default false;
}
