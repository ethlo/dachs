package com.ethlo.dachs.eclipselink;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.PersistenceUnitUtil;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.queries.WriteObjectQuery;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.AuditIgnore;
import com.ethlo.dachs.EntityEventListener;
import com.ethlo.dachs.EntityListenerAdapter;
import com.ethlo.dachs.InternalAuditEntityListener;
import com.ethlo.dachs.PropertyChange;

/**
 * 
 * @author mha
 *
 */
public class EclipseLinkAuditingLoggerHandler extends EntityListenerAdapter implements EntityEventListener<DescriptorEvent>, TransactionSynchronization
{
	private InternalAuditEntityListener auditLogger;
	
	private PersistenceUnitUtil persistenceUnitUtil;

	public EclipseLinkAuditingLoggerHandler(InternalAuditEntityListener auditLogger, PersistenceUnitUtil persistenceUnitUtil)
	{
	    this.auditLogger = auditLogger;
	    this.persistenceUnitUtil = persistenceUnitUtil;
	    
	    TransactionSynchronizationManager.registerSynchronization(this);
	}
	
	@Override
	public void postPersistEvent(DescriptorEvent event)
	{
		this.auditLogger.create(getObjectId(event.getObject()), event.getObject(), extractEntityProperties(event));
	}

	@Override
	public void postRemoveEvent(DescriptorEvent event) 
	{
		this.auditLogger.delete(getObjectId(event.getObject()), event.getObject());
	}
	
	@Override
	public void postUpdateEvent(DescriptorEvent event)
	{
		final Serializable key = getObjectId(event.getObject());
		final Object entity = event.getObject();
		final Collection<PropertyChange<?>> properties = handleModification(event);
		this.auditLogger.update(key, entity, properties);
	}
	
	private List<PropertyChange<?>> handleModification(DescriptorEvent event)
	{
		final Class<?> objectClass = event.getSource().getClass();
		final ObjectChangeSet changeset = ((WriteObjectQuery)event.getQuery()).getObjectChangeSet();
		final ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(event.getObject());
		
		final List<ChangeRecord> changes = changeset.getChanges();
		final List<PropertyChange<?>> propChanges = new ArrayList<PropertyChange<?>>();
		for (ChangeRecord change : changes)
		{
			final String attrName = change.getAttribute();
			final Object newValue = accessor.getPropertyValue(attrName);
			final Object oldValue = change.getOldValue();
			final Class<?> attrType = accessor.getPropertyType(attrName);
			
			if (ReflectionUtils.findField(objectClass, change.getAttribute()).getAnnotation(AuditIgnore.class) != null)
			{
				// Ignored
				continue;
			}
			
			if (Iterable.class.isAssignableFrom(attrType))
			{
				extractListDiff(propChanges, Iterable.class, attrName, newValue, oldValue);
			}
			else 
			{
				extractSingle(attrName, attrType, oldValue, newValue, propChanges);
			}
		}
		return propChanges;
	}
	
	private List<PropertyChange<?>> extractEntityProperties(DescriptorEvent event)
	{
		final List<PropertyChange<?>> propChanges = new ArrayList<PropertyChange<?>>();
		final Map<String, Field> fieldMap = new HashMap<String, Field>();
		final Object target = event.getObject();
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
	private <T> void extractChangeData(final List<PropertyChange<?>> propChanges, final Object target, Field field)
	{
		if (field.getAnnotation(AuditIgnore.class) != null)
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
		else if (field.getAnnotation(Column.class) != null)
		{
			if (value != null)
			{
				propChanges.add(new PropertyChange<Object>(fieldName, null, null, value));
			}
		}
		else if (field.getAnnotation(JoinColumn.class) != null)
		{
			if (value != null)
			{
				final Object id = persistenceUnitUtil.getIdentifier(value);
				propChanges.add(new PropertyChange(fieldName, id.getClass(), null, id));
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> void extractListDiff(final List<PropertyChange<?>> propChanges, Class<?> type, final String attrName, final Object newValue, final Object oldValue)
	{
		final Iterable<T> newList = (Iterable<T>) newValue;
		final Iterable<T> oldList = (Iterable<T>) oldValue;		
		final List<Serializable> oldVal = extractChangeList(oldList);
		final List<Serializable> newVal = extractChangeList(newList);
		propChanges.add(new PropertyChange(attrName, type, oldVal, newVal));
	}

	private List<Serializable> extractChangeList(Iterable<? extends Object> objects)
	{
		final List<Serializable> retVal = new ArrayList<Serializable>();
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
					retVal.add(safeToString(n));
				}
			}
		}
		return retVal;
	}

	private void extractSingle(String attrName, Class<?> attrType, Object oldValue, Object newValue, List<PropertyChange<?>> propChanges)
	{
		PropertyChange<?> propChange = null;
		if (isEntity(attrType))
		{
			propChange = extractEntityReference(attrName, oldValue, newValue);
		}
		else
		{
			propChange = extractSimpleValue(attrName, oldValue, newValue);
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
	private PropertyChange<?> extractSimpleValue(String attrName, Object oldValue, Object newValue)
	{
		if (! Objects.equals(oldValue, newValue))
		{
			return new PropertyChange(attrName, null, oldValue, newValue);
		}
		return null;
	}

	private String safeToString(Object obj)
	{
		return obj != null ? obj.toString() : null;
	}

	private Serializable getObjectId(Object obj) 
	{
		if (obj == null || obj.getClass().getAnnotation(Entity.class) == null)
		{
			return null;
		}
		final Object entityId = persistenceUnitUtil.getIdentifier(obj);
		if (entityId != null)
		{
			if (entityId.getClass().isAssignableFrom(Serializable.class))
			{
				return (Serializable) entityId;
			}
			throw new IllegalArgumentException("entityId must be a Serializable");
		}
		return null;
	}
	     
    @Override
	public void resume()
	{
		this.auditLogger.start();
	}

	@Override
	public void afterCompletion(int status)
	{
		switch (status)
		{
			case TransactionSynchronization.STATUS_ROLLED_BACK:
				this.auditLogger.discard();
				break;
				
			case TransactionSynchronization.STATUS_COMMITTED:
				this.auditLogger.flush();
				break;
		}
	}

	@Override
	public void suspend(){}

	@Override
	public void flush() {}

	@Override
	public void beforeCommit(boolean readOnly) {}

	@Override
	public void beforeCompletion() {}

	@Override
	public void afterCommit() {}
}