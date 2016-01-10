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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.BoundaryEntityListenerBuffer;
import com.ethlo.dachs.EntityData;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.jpa.EntityUtil;

/**
 * 
 * @author mha
 *
 */
public class EclipseLinkAuditingLoggerHandler extends EntityListenerAdapter implements EntityEventListener<DescriptorEvent>, TransactionSynchronization
{
	private BoundaryEntityListenerBuffer boundaryEntityListenerBuffer;
	
	private PersistenceUnitUtil persistenceUnitUtil;

	private EntityUtil entityUtil;

	public EclipseLinkAuditingLoggerHandler(BoundaryEntityListenerBuffer boundaryEntityListenerBuffer, PersistenceUnitUtil persistenceUnitUtil)
	{
	    this.boundaryEntityListenerBuffer = boundaryEntityListenerBuffer;
	    this.persistenceUnitUtil = persistenceUnitUtil;
	    this.entityUtil = new EntityUtil(persistenceUnitUtil);
	    
	    TransactionSynchronizationManager.registerSynchronization(this);
	}
	
	@Override
	public void postPersistEvent(DescriptorEvent event)
	{
		this.boundaryEntityListenerBuffer.created(new EntityData(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event)));
	}

	@Override
	public void postRemoveEvent(DescriptorEvent event) 
	{
		this.boundaryEntityListenerBuffer.deleted(new EntityData(getObjectId(event.getObject()), event.getObject(), entityUtil.extractEntityProperties(event)));
	}
	
	@Override
	public void postUpdateEvent(DescriptorEvent event)
	{
		final Serializable key = getObjectId(event.getObject());
		final Object entity = event.getObject();
		final Collection<PropertyChange<?>> properties = handleModification(event);
		this.boundaryEntityListenerBuffer.updated(new EntityData(key, entity, properties));
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
		this.boundaryEntityListenerBuffer.start();
	}

	@Override
	public void afterCompletion(int status)
	{
		switch (status)
		{
			case TransactionSynchronization.STATUS_ROLLED_BACK:
				this.boundaryEntityListenerBuffer.discard();
				break;
				
			case TransactionSynchronization.STATUS_COMMITTED:
				this.boundaryEntityListenerBuffer.flush();
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