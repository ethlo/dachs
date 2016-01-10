package com.ethlo.dachs.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import com.ethlo.dachs.EntityListener;
import com.ethlo.dachs.EntityData;
import com.ethlo.dachs.PropertyChange;

public class HibernatePropertyChangeInterceptor extends EmptyInterceptor
{
	private static final long serialVersionUID = 4618098551981894684L;
	
	private EntityListener[] listeners;

	public HibernatePropertyChangeInterceptor(EntityListener... listeners)
	{
		this.listeners = listeners;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		final Collection<PropertyChange<?>> props = getProperties(entity, new Object[state.length], state, propertyNames, types);
		for (EntityListener listener : listeners)
		{
			listener.deleted(new EntityData(id, entity, props));
		}
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	{
		final Collection<PropertyChange<?>> props = getProperties(entity, state, new Object[state.length], propertyNames, types);
		for (EntityListener listener : listeners)
		{
			listener.created(new EntityData(id, entity, props));
		}
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) 
	{
		final Collection<PropertyChange<?>> props = getProperties(entity, currentState, previousState, propertyNames, types);
		for (EntityListener listener : listeners)
		{
			listener.updated(new EntityData(id, entity, props));
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<PropertyChange<?>> getProperties(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
	{
		final List<PropertyChange<?>> retVal = new ArrayList<>();
		for (int i = 0; i < propertyNames.length; i++)
		{
			if (! Objects.equals(previousState[i], currentState[i]))
			{
				final PropertyChange changed = new PropertyChange(propertyNames[i], types[i].getReturnedClass(), previousState[i], currentState[i]);
				retVal.add(changed);
			}
		}
		return retVal;
	}
}