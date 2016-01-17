package com.ethlo.dachs.eclipselink;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.PersistenceUnitUtil;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.queries.WriteObjectQuery;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.EntityDataChangeImpl;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.jpa.EntityUtil;

/**
 * 
 * @author mha
 *
 */
public class EclipseLinkAuditingLoggerHandler implements EntityEventListener<DescriptorEvent>
{
	private PersistenceUnitUtil persistenceUnitUtil;
	private EntityUtil entityUtil;
	private InternalEntityListener listener;

	public EclipseLinkAuditingLoggerHandler(PersistenceUnitUtil persistenceUnitUtil, InternalEntityListener internalEntityListener)
	{
		this.listener = internalEntityListener;
	    this.persistenceUnitUtil = persistenceUnitUtil;
	    this.entityUtil = new EntityUtil(persistenceUnitUtil);
	}
	
	@Override
	public void postPersistEvent(DescriptorEvent event)
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), false));
		listener.created(e);
	}

	@Override
	public void postRemoveEvent(DescriptorEvent event) 
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), true));
		listener.deleted(e);
	}
	
	@Override
	public void postUpdateEvent(DescriptorEvent event)
	{
		final Serializable key = getObjectId(event.getObject());
		final Object entity = event.getObject();
		final Collection<PropertyChange<?>> properties = handleModification(event);
		final EntityDataChange e = new EntityDataChangeImpl(key, entity, properties);
		
		listener.updated(e);
	}
	
	private List<PropertyChange<?>> handleModification(DescriptorEvent event)
	{
		final Class<?> objectClass = event.getSource().getClass();
		final WriteObjectQuery writeObjectQuery = (WriteObjectQuery)event.getQuery();
		final ObjectChangeSet changeset = writeObjectQuery.getObjectChangeSet();
		final ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(event.getObject());
		
		final List<ChangeRecord> changes = changeset != null ? changeset.getChanges() : extractManually(writeObjectQuery);
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
	
	private List<ChangeRecord> extractManually(WriteObjectQuery writeObjectQuery)
	{
		final Object oldObj = writeObjectQuery.getBackupClone();
		final Object newObj = writeObjectQuery.getObject();
		
		final ConfigurablePropertyAccessor accessorOld = PropertyAccessorFactory.forDirectFieldAccess(oldObj);
		final ConfigurablePropertyAccessor accessorNew = PropertyAccessorFactory.forDirectFieldAccess(newObj);
		final BeanWrapper beanAccessor = PropertyAccessorFactory.forBeanPropertyAccess(newObj);
		
		final List<ChangeRecord> retVal = new LinkedList<>();
		for (PropertyDescriptor desc : beanAccessor.getPropertyDescriptors())
		{
			final String propName = desc.getName();
			if (accessorNew.isReadableProperty(propName))
			{
				final Object newPropValue = accessorNew.getPropertyValue(propName);
				final Object oldPropValue = accessorOld.getPropertyValue(propName);
				if (! Objects.equals(newPropValue, oldPropValue))
				{
					final ChangeRecord c = new ChangeRecord()
					{
						@Override
						public ObjectChangeSet getOwner()
						{
							return null;
						}
						
						@Override
						public Object getOldValue()
						{
							return oldPropValue;
						}
						
						@Override
						public String getAttribute()
						{
							return propName;
						}
					};
					retVal.add(c);
				}
			}
		}
		return retVal;
	}

	private Serializable getObjectId(Object obj) 
	{
		if (obj == null || obj.getClass().getAnnotation(Entity.class) == null)
		{
			return null;
		}
		final Object entityId = persistenceUnitUtil.getIdentifier(obj);
		return (Serializable) entityId;
	}

	@Override
	public void prePersistEvent(DescriptorEvent event)
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), false));

		listener.preCreate(e);
	}

	@Override
	public void preRemoveEvent(DescriptorEvent event)
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), true));

		listener.preDelete(e);
	}

	@Override
	public void preUpdateEvent(DescriptorEvent event)
	{
		final Serializable key = getObjectId(event.getObject());
		final Object entity = event.getObject();
		final Collection<PropertyChange<?>> properties = handleModification(event);
		final EntityDataChange e = new EntityDataChangeImpl(key, entity, properties);
		
		listener.preUpdate(e);
	}
}