package net.sourceforge.pbeans.data;

public interface BeanUnmarshaller {
	public Object registerBean(Long id);
	public String getIDField();
	
	/**
	 * @param bean The bean.
	 * @param normalFieldName A field name, which may not match the case of fields known by store.
	 * @param fieldValue The field value as reported by the database.
	 */
	public void setProperty(Object bean, String normalFieldName, Object fieldValue);
}
