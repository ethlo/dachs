package com.ethlo.dachs.hibernate;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

public class HibernatePropertyChangeInterceptorBridge extends EmptyInterceptor implements org.hibernate.Interceptor
{
	private static final long serialVersionUID = 6375798402716022601L;

	private static HibernatePropertyChangeInterceptor instance;
	
	public static void setHibernatePropertyChangeInterceptor(HibernatePropertyChangeInterceptor instance)
	{
		HibernatePropertyChangeInterceptorBridge.instance = instance;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	{
		Assert.notNull(instance);
		instance.onDelete(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	{
		Assert.notNull(instance);
		return instance.onSave(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) 
	{
		Assert.notNull(instance);
		return instance.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}
}
