package net.sourceforge.pbeans.data;

public class BeanWrapper {
	public final Long id;
	public final Object bean;
	
	public BeanWrapper(Long id, Object bean) {
		this.id = id;
		this.bean = bean;
	}
}
