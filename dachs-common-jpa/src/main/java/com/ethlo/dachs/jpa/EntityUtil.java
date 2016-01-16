package com.ethlo.dachs.jpa;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.PersistenceUnitUtil;

import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.PropertyChange;

public class EntityUtil
{
	private final PersistenceUnitUtil persistenceUnitUtil;

	public EntityUtil(PersistenceUnitUtil persistenceUnitUtil)
	{
		this.persistenceUnitUtil = persistenceUnitUtil;
	}

	public List<PropertyChange<?>> extractEntityProperties(Object target)
	{
		final List<PropertyChange<?>> propChanges = new ArrayList<PropertyChange<?>>();
		final Map<String, Field> fieldMap = new HashMap<String, Field>();
		ReflectionUtils.doWithFields(target.getClass(), new ReflectionUtils.FieldCallback()
		{
			public void doWith(Field field)
			{
				final String fieldName = field.getName();
				if (! fieldMap.containsKey(fieldName))
				{
					field.setAccessible(true);
					fieldMap.put(fieldName, field);
					if (! Modifier.isTransient(field.getModifiers()))
					{
						extractChangeData(propChanges, target, field);
					}
				}
			}
		});
		return propChanges;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void extractListDiff(final List<PropertyChange<?>> propChanges, Class<?> type, final String attrName, final Object newValue, final Object oldValue)
	{
		final Iterable<T> newList = (Iterable<T>) newValue;
		final Iterable<T> oldList = (Iterable<T>) oldValue;		
		final List<Object> oldVal = extractChangeList(oldList);
		final List<Object> newVal = extractChangeList(newList);
		propChanges.add(new PropertyChange(attrName, type, oldVal, newVal));
	}

	private List<Object> extractChangeList(Iterable<? extends Object> objects)
	{
		final List<Object> retVal = new ArrayList<>();
		if (objects != null)
		{
			for (Object n : objects)
			{
				if (n.getClass().getAnnotation(Entity.class) != null)
				{
					retVal.add(extractEntityRef(n));
				}
				else
				{
					retVal.add(n);
				}
			}
		}
		return retVal;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> void extractChangeData(final List<PropertyChange<?>> propChanges, final Object target, Field field)
	{
		if (field.getAnnotation(EntityListenerIgnore.class) != null)
		{
			// Skip ignored fields
			return;
		}
		
		final String fieldName = field.getName();
		final Object value = ReflectionUtils.getField(field, target);
		
		if (field.getType().isAssignableFrom(Iterable.class))
		{
			extractListDiff(propChanges, field.getType(), fieldName, value, null);
		}
		else if (field.getAnnotation(JoinColumn.class) != null)
		{
			if (value != null)
			{
				final Object id = persistenceUnitUtil.getIdentifier(value);
				propChanges.add(new PropertyChange(fieldName, id.getClass(), null, id));
			}
		}
		else
		{
			if (value != null)
			{
				propChanges.add(new PropertyChange(fieldName, field.getType(), null, value));
			}
		}
	}
	
	public void extractSingle(String attrName, Class<?> attrType, Object oldValue, Object newValue, List<PropertyChange<?>> propChanges)
	{
		PropertyChange<?> propChange = null;
		if (isEntity(attrType))
		{
			propChange = extractEntityReference(attrName, oldValue, newValue);
		}
		else
		{
			propChange = extractSimpleValue(attrName, attrType, oldValue, newValue);
		}
		
		if (propChange != null)
		{
			propChanges.add(propChange);
		}
	}

	private boolean isEntity(Class<?> attrType)
	{
		return attrType.getAnnotation(Entity.class) != null;
	}

	private <T> PropertyChange<Serializable> extractEntityReference(String attrName, Object oldValue, Object newValue)
	{
		final Serializable oldRef = extractEntityRef(oldValue);
		final Serializable newRef = extractEntityRef(newValue);
		if (! Objects.equals(oldRef, newRef))
		{
			return new PropertyChange<Serializable>(attrName, Serializable.class, oldRef, newRef);
		}
		return null;
	}

	private Serializable extractEntityRef(Object value)
	{
		return (Serializable) (value != null ? persistenceUnitUtil.getIdentifier(value) : null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private PropertyChange<?> extractSimpleValue(String attrName, Class<?> type, Object oldValue, Object newValue)
	{
		if (! Objects.equals(oldValue, newValue))
		{
			return new PropertyChange(attrName, type, oldValue, newValue);
		}
		return null;
	}

}
