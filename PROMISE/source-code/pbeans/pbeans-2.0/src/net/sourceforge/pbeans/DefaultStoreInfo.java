package net.sourceforge.pbeans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.util.logging.*;

import net.sourceforge.pbeans.data.FieldDescriptor;
import net.sourceforge.pbeans.util.Hash;
import net.sourceforge.pbeans.annotations.*;

class DefaultStoreInfo implements StoreInfo {
	private final static Logger logger = Logger.getLogger(DefaultStoreInfo.class.getName());
	private final Class clazz;
    private final Map<String,PropertyDescriptor> propertyDescriptors = new HashMap<String,PropertyDescriptor>();
    private final Map<String,FieldDescriptor> fieldDescriptors = new HashMap<String,FieldDescriptor>();
    private final Map<String,PropertyDescriptor> propertyDescriptorsByNormalFieldName = new HashMap<String,PropertyDescriptor>();
    
	private final String requestedTableName;	
	private final Index[] indexes;
	private final Store store;
	private final boolean userManaged;
	private final boolean autoIncrement;
	private final boolean deleteFields;
	private final String idField;
	private final PropertyDescriptor[] propertyDescriptorsArray;
	private String tableName;
	
	public DefaultStoreInfo(Class clazz, Store store) throws StoreException {
		this.clazz = clazz;
		this.store = store;
		PersistentClass pc = (PersistentClass) clazz.getAnnotation(PersistentClass.class);
		if(pc == null) {
			throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @PersistentClass (or the Persistent interface). Annotations present are: " + java.util.Arrays.asList(clazz.getAnnotations()));
		}
		String tableName = pc.table();
		if("".equals(tableName)) {
			tableName = null;
		}
		if(tableName != null && tableName.length() > store.getMaxTableNameLength()) {
			throw new StoreException("Table name '" + tableName + "' is too long.");
		}
		this.requestedTableName = tableName;
		PropertyIndex[] propIndexes = pc.indexes();
		if(propIndexes == null) {
			propIndexes = new PropertyIndex[0];
		}
		Index[] indexes = new Index[propIndexes.length];
		for(int i = 0; i < indexes.length; i++) {
			PropertyIndex propIndex = propIndexes[i];
			indexes[i] = new Index(propIndex.unique(), propIndex.propertyNames(), propIndex.keyLength());
		}
		this.indexes = indexes;
		this.userManaged = pc.userManaged();
		this.autoIncrement = pc.autoIncrement();
		this.deleteFields = pc.deleteFields();
		this.idField = pc.idField();
    	try {
    		BeanInfo binfo = Introspector.getBeanInfo (clazz);
    		PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
    		this.propertyDescriptorsArray = pds;
    		for (int i = 0; i < pds.length; i++) {
    			PropertyDescriptor pd = pds[i];
    			//Method getter = pd.getReadMethod();
    			//Method setter = pd.getWriteMethod();
    			String pdName = pd.getName();
    			this.propertyDescriptors.put(pdName, pds[i]);
    			FieldDescriptor fd = this.getFieldDescriptorImpl(pd, store.getMaxColumnNameLength());    			
    			if(fd != null) {
    				if(fd.getName().equalsIgnoreCase(this.idField)) {
    					throw new StoreException("Property " + pd.getName() + " of class " + clazz.getName() + " results in a field name that is the same as the (implicit) ID field.");
    				}
    				this.fieldDescriptors.put(pdName, fd);
    				this.propertyDescriptorsByNormalFieldName.put(store.normalizeName(fd.getName()), pd);
    			}
    			else {
    				// Transient warnings already logged
    			}
    		}
    	} catch (IntrospectionException ie) {
    		throw new IllegalArgumentException ("Unable to instrospect class " + clazz.getName());
    	}		
	}
	
	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.propertyDescriptorsArray;
	}
	
	public String getIdField() {
		return this.idField;
	}

	public boolean isAutoIncrementRequested() {
		return this.autoIncrement;
	}

	public boolean isDeleteFields() {
		return this.deleteFields;
	}

	public Object create(Store store) throws StoreException {
    	try {
    		return this.clazz.newInstance();
    	} catch (InstantiationException ie) {
    		throw new StoreException ("Unable to instantiate class " + this.clazz.getName() + ". It should have a default public constructor, or it should have a _StoreInfo suffixed class which implements StoreInfo.");
    	} catch (IllegalAccessException iae) {
    		throw new StoreException (iae);
    	}
	}

	public ClassLoader getClassLoader() {
		return this.clazz.getClassLoader();
	}

	public FieldDescriptor getFieldDescriptor(Store store, PropertyDescriptor pd) throws StoreException {		
		return this.fieldDescriptors.get(pd.getName());
	}
	
	private static int getPreferredSQLType(Class propType, boolean isGlobalReference) {
		int sqlType;
		if(isGlobalReference) {
			sqlType = Types.VARCHAR;
		}
		else {
			if (String.class == propType) {
				sqlType = ( Types.VARCHAR);
			}
			else if (int.class == (propType)) {
				sqlType = ( Types.INTEGER);
			}
			else if (Integer.class == (propType)) {
				sqlType = ( Types.INTEGER);
			}
			else if (long.class == (propType)) {
				sqlType = ( Types.BIGINT);
			}
			else if (Long.class == (propType)) {
				sqlType = ( Types.BIGINT);
			}
			else if (java.sql.Date.class.isAssignableFrom (propType)) {
				sqlType = ( Types.DATE);
			}
			else if (java.util.Date.class == propType) {
				sqlType = ( Types.BIGINT);
			}
			else if (java.sql.Timestamp.class.isAssignableFrom (propType)) {
				sqlType = ( Types.TIMESTAMP);
			}
			else if (java.sql.Time.class.isAssignableFrom (propType)) {
				sqlType = ( Types.TIME);
			}
			else if(propType.isEnum()) {
				sqlType = Types.VARCHAR;
			}
			else if (PersistentID.class == propType) {
				sqlType = ( Types.BIGINT);
			}
			else if(Store.isPersistable(propType)) {
				sqlType = Types.BIGINT;
			}
			else if (boolean.class == (propType)) {
				sqlType = ( Types.BOOLEAN);
			}
			else if (Boolean.class == (propType)) {
				sqlType = ( Types.BOOLEAN);
			}
			else if (double.class == (propType)) {
				sqlType = ( Types.DOUBLE);
			}
			else if (Double.class == (propType)) {
				sqlType = ( Types.DOUBLE);
			}
			else if (float.class == (propType)) {
				sqlType = ( Types.FLOAT);
			}
			else if (Float.class == (propType)) {
				sqlType = ( Types.FLOAT);
			}
			else if (short.class == (propType)) {
				sqlType = ( Types.SMALLINT);
			}
			else if (Short.class == (propType)) {
				sqlType = ( Types.SMALLINT);
			}
			else if (char.class == (propType)) {
				sqlType = ( Types.CHAR);
			}
			else if (Character.class == (propType)) {
				sqlType = ( Types.CHAR);
			}
			else if (byte.class == (propType)) {
				sqlType = ( Types.SMALLINT);
			}
			else if (Byte.class == (propType)) {
				sqlType = ( Types.SMALLINT);
			}
			else if(GlobalPersistentID.class == propType) {
				sqlType = Types.VARCHAR;
			}
			else if(propType.isArray()) {
				Class elementType = propType.getComponentType();
				// globalReference element may also refer to array component types.
				if(elementType == byte.class || elementType == boolean.class || elementType == char.class || elementType == short.class || elementType == int.class || elementType == long.class || Store.isPersistable(elementType)) {
					sqlType = Types.BLOB;
				}
				else {
					// Unsupported
					sqlType = 0;
				}
			}
			else {
				// Unsupported
				sqlType = 0;
			}		
		}
		return sqlType;
	}
	
	private FieldDescriptor getFieldDescriptorImpl(PropertyDescriptor pd, int maxColumnLength) throws StoreException {		
		Method getter = pd.getReadMethod();
		Method setter = pd.getWriteMethod();
		if((getter != null && getter.isAnnotationPresent(TransientProperty.class)) ||
			(setter != null && setter.isAnnotationPresent(TransientProperty.class))) {
			if(logger.isLoggable(Level.INFO)) {
				logger.info("getFieldDescriptor(): Property " + pd.getName() + " of class " + this.clazz.getName() + " is marked transient.");
			}
			return null;
		}
		if(getter == null || setter == null) {
			// Not a valid property
			if(logger.isLoggable(Level.WARNING) && !"class".equals(pd.getName())) {
				logger.warning("getFieldDescriptor(): Property " + pd.getName() + " of class " + this.clazz.getName() + " does not have a getter and a setter.");
			}
			return null;
		}
		PersistentProperty ppa = getter.getAnnotation(PersistentProperty.class);
		if(ppa == null) {
			ppa = setter.getAnnotation(PersistentProperty.class);
		}
		String requestedFieldName = ppa == null ? null : ppa.field();
		if("".equals(requestedFieldName)) {
			requestedFieldName = null;
		}
		if(requestedFieldName != null && requestedFieldName.length() > maxColumnLength) {
			throw new StoreException("Field name '" + requestedFieldName + "' is too long.");
		}
    	String fieldName = requestedFieldName == null ? store.getShortColumnName(pd.getName(), "F") : requestedFieldName;
		boolean isGlobalReference = ppa == null ? false : ppa.globalReference();
    	int sqlType = 0; //ppa == null ? 0 : ppa.sqlType();
    	if(sqlType == 0) {
			Class propType = pd.getPropertyType();
			sqlType = DefaultStoreInfo.getPreferredSQLType(propType, isGlobalReference);
			if(sqlType == 0) {
				String extra = "";
				if(propType == Object.class) {
					extra = " See also @PersistentProperty.globalReference.";
				}
				throw new StoreException("Property " + pd.getName() + " should be annotated with @TransientProperty as its type is unsupported for automatic persistence." + extra);
			}
    	}
    	boolean nullable = ppa == null ? true : ppa.nullable();
    	String renamedFrom = ppa == null ? null : ppa.renamedFrom();
    	if("".equals(renamedFrom)) {
    		renamedFrom = null;
    	}
    	return new FieldDescriptor(fieldName, sqlType, nullable, renamedFrom, isGlobalReference);
	}

	public Index[] getIndexes(Store store) throws StoreException {
		return this.indexes;
	}

	public PropertyDescriptor getPropertyDescriptor(String propertyName) {
    	return this.propertyDescriptors.get(propertyName);
	}
	
    public Class getBeanClass() {
    	return this.clazz;
	}

    /**
     * @param normalFieldName A case-insensitive field name. 
     */
    public PropertyDescriptor getPropertyDescriptorByNormalFieldName(Store store, String normalFieldName) {
		return this.propertyDescriptorsByNormalFieldName.get(normalFieldName);
	}

	public boolean isUserManaged() {
		return this.userManaged;
	}

	public String getTableName(int maxLength) {
    	synchronized(this) {
    		String tableName = this.tableName;
    		if(tableName != null) {
    			return tableName;
    		}
    		tableName = this.requestedTableName;
    		if(tableName != null) {
    			this.tableName = tableName;
    			return tableName;
    		}
    		String prefix = "T";
    		String className = clazz.getName();
    		String tName = className.replace('.','_').replace('$','_');
    		int totalLength = prefix.length() + tName.length();
    		if (totalLength <= maxLength) {
    			tableName = prefix + tName;
    		}
    		else {
    			// Note: Don't change what the fixedHash is obtained from.
    			int hash = Math.abs(Hash.fixedHash (className));
    			String hashStr = String.valueOf(hash);
    			int canUseLength = maxLength - prefix.length() - hashStr.length();
    			tableName =
    				prefix + 
    				hashStr + 
    				tName.substring(
    						tName.length() - canUseLength, 
    						tName.length());

    		}
    		this.tableName = tableName;
        	return tableName;
    	}
    }

	/**
	 * Marshalls a property value into one that can be set in JDBC.
	 * Generally this method should return the property value, unchanged, except
	 * to convert persistent IDs and so on. The database implementation
	 * is expected to handle any additional conversions.
	 */
    public Object marshallValue(PropertyDescriptor pd, Object propertyValue) throws StoreException {
    	Class propertyType = pd.getPropertyType();
    	if (propertyValue instanceof String) {
    		return propertyValue;
    	}
    	else if(propertyValue instanceof Long) {
    		return propertyValue;
    	}
    	else if(propertyValue instanceof Integer) {
    		return propertyValue;
    	}
    	else if(propertyValue instanceof Enum) {
    		return ((Enum) propertyValue).name();
    	}
    	else if (propertyValue == null) {
    		return null;
    	}
    	else if (PersistentID.class == propertyType) {
    		return ((PersistentID) propertyValue).longValue();
    	}
    	else if (java.util.Date.class == propertyType) {
    		return new Long(((java.util.Date) propertyValue).getTime());
    	}
    	else if (GlobalPersistentID.class == propertyType) {
    		return ((GlobalPersistentID) propertyValue).toString();
    	}
    	else if(propertyType.isArray()) {
    		byte[] bytes = DefaultStoreInfo.convertToByteArray(propertyValue);
    		return bytes;
    	}
    	else {
    		return propertyValue;
    	}
    }
    
    /**
     * Unmarshalls a field value into a property value. This method
     * does need to handle various possible conversions, because
     * Java definitions might have changed, whereas database definitions
     * have not changed necessarily. This method should ideally not give
     * errors due to unanticipated database types.
     */
    public Object unmarshallValue (PropertyDescriptor pd, Object fieldValue) throws StoreException {
    	Class propertyType = pd.getPropertyType();
    	if (String.class == propertyType) {
    		if(fieldValue instanceof String) {
    			return fieldValue;
    		}
    		else if(fieldValue == null) {
    			return null;
    		}
    		else {
    			return String.valueOf(fieldValue);
    		}
    	}
    	else if (long.class == propertyType || Long.class == propertyType || Store.isPersistable(propertyType)) {
    		if(fieldValue instanceof Long) {
    			return fieldValue;
    		}
    		else if(fieldValue == null) {
    			return null;
    		}
    		else if(fieldValue instanceof Integer) {
    			return new Long(((Integer) fieldValue).intValue());
    		}
    		else if(fieldValue instanceof Double) {
    			return new Long((long) ((Double) fieldValue).doubleValue());
    		}
    		else {
    			return Long.valueOf(String.valueOf(fieldValue));
    		}
    	}
    	else if (int.class == propertyType || Integer.class == propertyType) {
    		if(fieldValue instanceof Integer) {
    			return fieldValue;
    		}
    		else if(fieldValue == null) {
    			return null;
    		}
    		else if(fieldValue instanceof Long) {
    			return new Integer(((Long) fieldValue).intValue());
    		}
    		else if(fieldValue instanceof Double) {
    			return new Integer((int) ((Double) fieldValue).doubleValue());
    		}
    		else {
    			return Integer.valueOf(String.valueOf(fieldValue));
    		}
    	}
    	else if(propertyType.isEnum()) {
    		if(fieldValue instanceof String) {
    			return Enum.valueOf(propertyType, (String) fieldValue);
    		}
    		else {
    			return Enum.valueOf(propertyType, String.valueOf(fieldValue));
    		}
    	}
    	else if (PersistentID.class == propertyType) {
    		if(!(fieldValue instanceof Long)) {
    			logger.warning("unmarshallValue(): Expected field value for property " + pd.getName() + " to be of type Long.");
    			return null;
    		}
    		else {
    			return new PersistentID (((Long) fieldValue).longValue());
    		}    			
    	}
    	else if (fieldValue == null) {
    		return null;
    	}
    	else if (java.util.Date.class == propertyType) {
    		if(fieldValue instanceof java.util.Date) {
    			return fieldValue;
    		}
    		else if(fieldValue instanceof Long) {
    			return new java.util.Date(((Long) fieldValue).longValue());
    		}
    		else if(fieldValue == null) {
    			return null;
    		}
    		else {
    			try {
    				return new java.text.SimpleDateFormat().parse(String.valueOf(fieldValue));
    			} catch(java.text.ParseException pe) {
    				logger.warning("Unable to parse date: " + fieldValue);
    				return null;
    			}
    		}
    	}    		
    	else if(boolean.class == propertyType || Boolean.class == propertyType) {
    		if(fieldValue instanceof Integer) {
    			return ((Integer) fieldValue).intValue() != 0;
    		}
    		else if(fieldValue instanceof Byte) {
    			return ((Byte) fieldValue).intValue() != 0;    			
    		}
    		else if(fieldValue instanceof Short) {
    			return ((Short) fieldValue).intValue() != 0;    			
    		}
    		else if(fieldValue instanceof Long) {
    			return ((Long) fieldValue).intValue() != 0;    			
    		}
    		else {
    			return fieldValue;
    		}
    	}
    	else if (double.class == propertyType || java.lang.Double.class == propertyType) {
    		if(fieldValue instanceof Double) {
    			return fieldValue;
    		}
    		else if(fieldValue instanceof Float) {
    			return new Double(((Float) fieldValue).floatValue());
    		}
    		else if(fieldValue == null) {
    			return null;
    		}
    		else if(fieldValue instanceof Long) {
    			return new Double(((Long) fieldValue).longValue());
    		}
    		else if(fieldValue instanceof Integer) {
    			return new Double(((Integer) fieldValue).intValue());
    		}
    		else {
    			return Double.valueOf(String.valueOf(fieldValue));
    		}
    	}
    	else if(byte.class == propertyType || Byte.class == propertyType) {
    		if(fieldValue instanceof Byte) {
    			return fieldValue; 			
    		}
    		else if(fieldValue instanceof Integer) {
    			return ((Integer) fieldValue).byteValue();
    		}
    		else if(fieldValue instanceof Short) {
    			return ((Short) fieldValue).byteValue();
    		}
    		else if(fieldValue instanceof Long) {
    			return ((Long) fieldValue).byteValue();
    		}
    		else {
    			return fieldValue;
    		}
    	}
    	else if (float.class == propertyType || java.lang.Float.class == propertyType) {
    		if(fieldValue instanceof Float) {
    			return fieldValue;
    		}
    		else if(fieldValue instanceof Double) {
    			return new Float((float) ((Double) fieldValue).doubleValue());
    		}
    		else if(fieldValue == null) {
    			return null;
    		}
    		else if(fieldValue instanceof Long) {
    			return new Float(((Long) fieldValue).longValue());
    		}
    		else if(fieldValue instanceof Integer) {
    			return new Float(((Integer) fieldValue).intValue());
    		}
    		else {
    			return Float.valueOf(String.valueOf(fieldValue));
    		}
    	}
    	else if(propertyType.isArray()) {
    		if(!(fieldValue instanceof byte[])) {
    			logger.warning("unmarshallValue(): Expected field value for property " + pd.getName() + " to be of type byte[].");
    			return null;
    		}
    		byte[] blob = (byte[]) fieldValue;
    		return DefaultStoreInfo.convertFromByteArray(blob, propertyType);
    	}
    	else if (fieldValue instanceof byte[] && (Character.class == propertyType || char.class == propertyType)) {
    		try {
    			String s = new String((byte[]) fieldValue, "UTF-8");
    			return new Character(s.charAt(0));
    		} catch (UnsupportedEncodingException ue) {
    			throw new IllegalArgumentException (ue.getMessage());
    		}
    	}
    	else if ((short.class == propertyType || java.lang.Short.class == propertyType) && fieldValue instanceof Integer) {
    		return new Short((short) ((Integer) fieldValue).intValue());
    	}
    	else if ((byte.class == propertyType || java.lang.Byte.class == propertyType) && fieldValue instanceof Integer) {
    		return new Byte((byte) ((Integer) fieldValue).intValue());
    	}
    	else if ((char.class == propertyType || java.lang.Character.class == propertyType)) {
    		if(fieldValue instanceof Character) {
    			return fieldValue;
    		}
    		else if(fieldValue instanceof String) {
    			try {
    				return ((String) fieldValue).charAt(0);
    			} catch(java.lang.IndexOutOfBoundsException iob) {
    				logger.warning("Character field value for property " + pd.getName() + " was a blank string.");
    				return (char) 0;
    			}
    		}
    		else {
    			return fieldValue;
    		}
    	}
    	else if(GlobalPersistentID.class == propertyType) {
    		if(!(fieldValue instanceof String)) {
    			logger.warning("unmarshallValue(): Expected field value for property " + pd.getName() + " to be of type String.");
    			return null;
    		}
    		else {
    			return GlobalPersistentID.valueOf((String) fieldValue);
    		}
    	}
    	else {
    		return fieldValue;
    	}
    }
    
    private static byte[] convertToByteArray(Object array) throws StoreException {
    	if(array instanceof byte[]) {
    		return (byte[]) array;
    	}
    	if(array instanceof boolean[]) {
    		boolean[] source = (boolean[]) array;
    		byte[] dest = new byte[source.length];
    		int destidx = 0;
    		for(int i = 0; i < source.length; i++) {
    			dest[destidx++] = (byte) (source[i] ? 1 : 0);
    		}
    		return dest;
    	}
    	else if(array instanceof char[]) {
    		char[] source = (char[]) array;
    		byte[] dest = new byte[source.length * 2];
    		int destidx = 0;
    		for(int i = 0; i < source.length; i++) {
    			dest[destidx++] = (byte) ((source[i] & 0xFF00) >> 8);
    			dest[destidx++] = (byte) (source[i] & 0xFF);
    		}
    		return dest;
    	}
    	else if(array instanceof short[]) {
    		short[] source = (short[]) array;
    		byte[] dest = new byte[source.length * 2];
    		int destidx = 0;
    		for(int i = 0; i < source.length; i++) {
    			dest[destidx++] = (byte) ((source[i] & 0xFF00) >> 8);
    			dest[destidx++] = (byte) (source[i] & 0xFF);
    		}    		
    		return dest;
    	}
    	else if(array instanceof int[]) {
    		int[] source = (int[]) array;
    		byte[] dest = new byte[source.length * 4];
    		int destidx = 0;
    		for(int i = 0; i < source.length; i++) {
    			dest[destidx++] = (byte) ((source[i] & 0xFF000000) >>> 24);
    			dest[destidx++] = (byte) ((source[i] & 0xFF0000) >> 16);
    			dest[destidx++] = (byte) ((source[i] & 0xFF00) >> 8);
    			dest[destidx++] = (byte) (source[i] & 0xFF);
    		}    		
    		return dest;
    	}
    	else if(array instanceof long[]) {
    		long[] source = (long[]) array;
    		byte[] dest = new byte[source.length * 8];
    		int destidx = 0;
    		for(int i = 0; i < source.length; i++) {
    			dest[destidx++] = (byte) ((source[i] & 0xFF00000000000000L) >>> 56);
    			dest[destidx++] = (byte) ((source[i] & 0xFF000000000000L) >> 48);
    			dest[destidx++] = (byte) ((source[i] & 0xFF0000000000L) >> 40);
    			dest[destidx++] = (byte) ((source[i] & 0xFF00000000L) >> 32);
    			dest[destidx++] = (byte) ((source[i] & 0xFF000000L) >>> 24);
    			dest[destidx++] = (byte) ((source[i] & 0xFF0000L) >> 16);
    			dest[destidx++] = (byte) ((source[i] & 0xFF00L) >> 8);
    			dest[destidx++] = (byte) (source[i] & 0xFFL);
    		}    		
    		return dest;
    	}
    	else {
    		throw new StoreException("Marshalling of arrays of type " + array.getClass().getSimpleName() + " is not supported.");
    	}
    }

    private static Object convertFromByteArray(byte[] blob, Class propertyType) throws StoreException {
    	Class componentType = propertyType.getComponentType();
    	if(componentType == byte.class) {
    		return blob;
    	}
    	if(componentType == boolean.class) {
    		boolean[] dest = new boolean[blob.length];
    		for(int i = 0; i < dest.length; i++) {
    			dest[i] = blob[i] != 0;
    		}
    		return dest;    		
    	}
    	else if(componentType == char.class) {
    		char[] dest = new char[blob.length / 2];
    		int srcidx = 0;
    		for(int i = 0; i < dest.length; i++) {
    			dest[i] = (char) (((blob[srcidx++] & 0xFF) << 8) | (blob[srcidx++] & 0xFF));
    		}
    		return dest;
    	} 
    	else if(componentType == short.class) {
    		short[] dest = new short[blob.length / 2];
    		int srcidx = 0;
    		for(int i = 0; i < dest.length; i++) {
    			dest[i] = (short) (((blob[srcidx++] & 0xFF) << 8) | (blob[srcidx++] & 0xFF));
    		}
    		return dest;    		
    	}
    	else if(componentType == int.class) {
    		int[] dest = new int[blob.length / 4];
    		int srcidx = 0;
    		for(int i = 0; i < dest.length; i++) {
    			dest[i] = ((blob[srcidx++] & 0xFF) << 24) | ((blob[srcidx++] & 0xFF) << 16) | ((blob[srcidx++] & 0xFF) << 8) | (blob[srcidx++] & 0xFF);
    		}
    		return dest;    		
    	}
    	else if(componentType == long.class) {
    		long[] dest = new long[blob.length / 8];
    		int srcidx = 0;
    		for(int i = 0; i < dest.length; i++) {
    			dest[i] = ((blob[srcidx++] & 0xFFL) << 56) | ((blob[srcidx++] & 0xFFL) << 48) | ((blob[srcidx++] & 0xFFL) << 40) | ((blob[srcidx++] & 0xFFL) << 32) | ((blob[srcidx++] & 0xFFL) << 24) | ((blob[srcidx++] & 0xFFL) << 16) | ((blob[srcidx++] & 0xFFL) << 8) | (blob[srcidx++] & 0xFFL);
    		}
    		return dest;    		
    	}
    	else if(Store.isPersistable(componentType)) {
    		// Return long[] here; Store takes care of the rest.
    		return DefaultStoreInfo.convertFromByteArray(blob, long[].class);
    	}
    	else {
    		throw new StoreException("Unmarshalling of arrays to type " + propertyType.getSimpleName() + " is not supported.");
    	}
    }
}
