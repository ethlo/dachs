package com.ethlo.dachs.eclipselink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PersistenceUnitUtil;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.queries.WriteObjectQuery;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.EntityData;
import com.ethlo.dachs.EntityDataImpl;
import com.ethlo.dachs.EntityListener;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.jpa.EntityUtil;

/**
 * 
 * @author mha
 *
 */
public class EclipseLinkAuditingLoggerHandler extends EntityListenerAdapter implements EntityEventListener<DescriptorEvent>
{
	private PersistenceUnitUtil persistenceUnitUtil;
	private EntityUtil entityUtil;
	private EntityListener[] entityListeners;

	public EclipseLinkAuditingLoggerHandler(PersistenceUnitUtil persistenceUnitUtil, EntityListener... entityListeners)
	{
		this.entityListeners = entityListeners;
	    this.persistenceUnitUtil = persistenceUnitUtil;
	    this.entityUtil = new EntityUtil(persistenceUnitUtil);
	}
	
	@Override
	public void postPersistEvent(DescriptorEvent event)
	{
		final EntityData e = new EntityDataImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event));
		for (EntityListener listener : entityListeners)
		{
			listener.created(e);
		}
	}

	@Override
	public void postRemoveEvent(DescriptorEvent event) 
	{
		final EntityData e = new EntityDataImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event));
		for (EntityListener listener : entityListeners)
		{
			listener.deleted(e);
		}
	}
	
	@Override
	public void postUpdateEvent(DescriptorEvent event)
	{
		final Serializable key = getObjectId(event.getObject());
		final Object entity = event.getObject();
		final Collection<PropertyChange<?>> properties = handleModification(event);
		final EntityData e = new EntityDataImpl(key, entity, properties);
		
		for (EntityListener listener : entityListeners)
		{
			listener.updated(e);
		}
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
			
			if (ReflectionUtils.findField(objectClass, change.getAttribute()).getAnnotation(EntityListenerIgnore.class) != null)
			{
				// Ignored
				continue;
			}
			
			if (Iterable.class.isAssignableFrom(attrType))
			{
				entityUtil.extractListDiff(propChanges, Iterable.class, attrName, newValue, oldValue);
			}
			else 
			{
				entityUtil.extractSingle(attrName, attrType, oldValue, newValue, propChanges);
			}
		}
		return propChanges;
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
			if (Serializable.class.isAssignableFrom(entityId.getClass()))
			{
				return (Serializable) entityId;
			}
			throw new IllegalArgumentException("entityId must be a Serializable");
		}
		return null;
	}
}