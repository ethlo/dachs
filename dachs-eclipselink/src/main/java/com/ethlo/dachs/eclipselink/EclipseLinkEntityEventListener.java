package com.ethlo.dachs.eclipselink;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.jpa.EntityUtil;

/**
 * 
 * @author mha
 *
 */
public class EclipseLinkEntityEventListener implements EntityEventListener<DescriptorEvent>
{
	private final PersistenceUnitUtil persistenceUnitUtil;
	private final EntityUtil entityUtil;
	private final InternalEntityListener listener;
	private final Predicate<Field> fieldFilter;
    private final Predicate<Object> entityFilter;

	public EclipseLinkEntityEventListener(PersistenceUnitUtil persistenceUnitUtil, InternalEntityListener internalEntityListener)
	{
	    this.persistenceUnitUtil = persistenceUnitUtil;
		this.listener = internalEntityListener;
	    this.entityUtil = new EntityUtil(persistenceUnitUtil);
	    
	    this.entityFilter = internalEntityListener.getEntityFilter();
	    this.fieldFilter = internalEntityListener.getFieldFilter();
	}
	
	@Override
	public void postPersistEvent(DescriptorEvent event)
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), false, entityFilter, fieldFilter));
		listener.created(e);
	}

	@Override
	public void postRemoveEvent(DescriptorEvent event) 
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), true, entityFilter, fieldFilter));
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
		
		final List<ChangeRecord> changes = changeset != null ? changeset.getChanges() : extractManually(writeObjectQuery, fieldFilter);
		final List<PropertyChange<?>> propChanges = new ArrayList<PropertyChange<?>>();
		for (ChangeRecord change : changes)
		{
			final String attrName = change.getAttribute();
			final Object newValue = accessor.getPropertyValue(attrName);
			final Object oldValue = change.getOldValue();
			final Class<?> attrType = accessor.getPropertyType(attrName);
			
			final Field field = ReflectionUtils.findField(objectClass, change.getAttribute());
			if (! fieldFilter.test(field))
			{
			    continue;
			}
			
			entityUtil.extractSingle(attrName, attrType, oldValue, newValue, propChanges);
		}
		return propChanges;
	}
	
	private List<ChangeRecord> extractManually(WriteObjectQuery writeObjectQuery, Predicate<Field> filter)
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
			    final Field field = ReflectionUtils.findField(oldObj.getClass(), propName);
			    if (filter.test(field))
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
		}
		return retVal;
	}

	private Serializable getObjectId(Object obj) 
	{
		final Object entityId = persistenceUnitUtil.getIdentifier(obj);
		return (Serializable) entityId;
	}

	@Override
	public void prePersistEvent(DescriptorEvent event)
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), false, entityFilter, fieldFilter));

		listener.preCreate(e);
	}

	@Override
	public void preRemoveEvent(DescriptorEvent event)
	{
		final EntityDataChange e = new EntityDataChangeImpl(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event.getObject(), true, entityFilter, fieldFilter));

		listener.preDelete(e);
	}

	@Override
	public void preUpdateEvent(DescriptorEvent event)
	{
		final Serializable key = getObjectId(event.getObject());
		final Object entity = event.getObject();
		if (entityFilter.test(entity))
		{
		    final Collection<PropertyChange<?>> properties = handleModification(event);
		    final EntityDataChange e = new EntityDataChangeImpl(key, entity, properties);
		    listener.preUpdate(e);
		}
	}
}