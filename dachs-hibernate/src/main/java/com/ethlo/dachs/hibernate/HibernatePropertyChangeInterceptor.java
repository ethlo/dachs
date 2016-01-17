package com.ethlo.dachs.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.EntityDataChangeImpl;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.PropertyChange;

public class HibernatePropertyChangeInterceptor extends EmptyInterceptor
{
	private static final long serialVersionUID = 4618098551981894684L;
	
	private InternalEntityListener listener;

	public HibernatePropertyChangeInterceptor(InternalEntityListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		final Collection<PropertyChange<?>> props = getProperties(entity, new Object[state.length], state, propertyNames, types);
		listener.deleted(new EntityDataChangeImpl(id, entity, props));
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	{
		final Collection<PropertyChange<?>> props = getProperties(entity, state, new Object[state.length], propertyNames, types);
		listener.created(new EntityDataChangeImpl(id, entity, props));
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) 
	{
		final Collection<PropertyChange<?>> props = getProperties(entity, currentState, previousState, propertyNames, types);
		listener.updated(new EntityDataChangeImpl(id, entity, props));
		return false;
	}
	
	private boolean isIgnored(Object entity, String propertyName)
	{
		final Field field = ReflectionUtils.findField(entity.getClass(), propertyName);
		if (field == null)
		{
			return true;
		}
		return (field.getAnnotation(EntityListenerIgnore.class) != null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<PropertyChange<?>> getProperties(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
	{
		final List<PropertyChange<?>> retVal = new ArrayList<>();
		for (int i = 0; i < propertyNames.length; i++)
		{
			final String propertyName = propertyNames[i];
			if (!isIgnored(entity, propertyName) 
				&& !Objects.equals(previousState[i], currentState[i]))
			{
				final PropertyChange changed = new PropertyChange(propertyName, types[i].getReturnedClass(), previousState[i], currentState[i]);
				retVal.add(changed);
			}
		}
		return retVal;
	}
}