package com.ethlo.dachs.eclipselink;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
 * @author mha
 */
public class EclipseLinkEntityEventListener implements EntityEventListener<DescriptorEvent>
{
    private final PersistenceUnitUtil persistenceUnitUtil;
    private final EntityUtil entityUtil;
    private final InternalEntityListener listener;
    private final Predicate<Field> fieldFilter;
    private final Predicate<Object> entityFilter;

    private final ConcurrentMap<String, PropertyAccessorCache> propertyAccessorCache = new ConcurrentHashMap<>();

    public EclipseLinkEntityEventListener(PersistenceUnitUtil persistenceUnitUtil, InternalEntityListener internalEntityListener)
    {
        this.persistenceUnitUtil = persistenceUnitUtil;
        this.listener = internalEntityListener;
        this.entityUtil = new EntityUtil();

        this.entityFilter = internalEntityListener.entityFilter();
        this.fieldFilter = internalEntityListener.fieldFilter();
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

        final WriteObjectQuery writeObjectQuery = (WriteObjectQuery) event.getQuery();
        final ObjectChangeSet changeset = writeObjectQuery.getObjectChangeSet();
        final ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(event.getObject());

        final List<ChangeRecord> changes = changeset != null ? changeset.getChanges() : extractManually(writeObjectQuery, fieldFilter);
        final List<PropertyChange<?>> propChanges = new ArrayList<>();
        for (ChangeRecord change : changes)
        {
            final String attrName = change.getAttribute();
            final Object newValue = accessor.getPropertyValue(attrName);
            final Object oldValue = change.getOldValue();
            final Class<?> attrType = accessor.getPropertyType(attrName);

            final Field field = ReflectionUtils.findField(objectClass, change.getAttribute());
            if (fieldFilter != null && !fieldFilter.test(field))
            {
                continue;
            }

            entityUtil.extractSingle(attrName, attrType, oldValue, newValue, propChanges);
        }
        return propChanges;
    }

    private List<ChangeRecord> extractManually(WriteObjectQuery writeObjectQuery, Predicate<Field> filter)
    {
        final PropertyAccessorCache accessorCache = getAccessorCache(writeObjectQuery.getObject(), filter);

        final Object oldObj = writeObjectQuery.getBackupClone();
        final Object newObj = writeObjectQuery.getObject();

        final List<ChangeRecord> retVal = new LinkedList<>();
        for (Map.Entry<String, Field> entry : accessorCache.getFields().entrySet())
        {
            final String propName = entry.getKey();
            final Object newPropValue = accessorCache.getValue(propName, newObj);
            final Object oldPropValue = accessorCache.getValue(propName, oldObj);
            if (!Objects.equals(newPropValue, oldPropValue))
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
        return retVal;
    }

    private PropertyAccessorCache getAccessorCache(final Object bean, final Predicate<Field> filter)
    {
        final Class<?> type = bean.getClass();
        final String typeName = type.getName();
        return propertyAccessorCache.computeIfAbsent(typeName, (missing) ->
        {
            final BeanWrapper beanAccessor = PropertyAccessorFactory.forBeanPropertyAccess(bean);
            final Map<String, Field> accessibleFields = new HashMap<>();
            for (PropertyDescriptor desc : beanAccessor.getPropertyDescriptors())
            {
                final String propName = desc.getName();
                if (beanAccessor.isReadableProperty(propName))
                {
                    final Field field = ReflectionUtils.findField(type, propName);
                    if (field != null)
                    {
                        if (filter == null || filter.test(field))
                        {
                            field.setAccessible(true);
                            accessibleFields.put(propName, field);
                        }
                    }
                }
            }
            return new PropertyAccessorCache(type, accessibleFields);
        });
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
        if (entityFilter == null || entityFilter.test(entity))
        {
            final Collection<PropertyChange<?>> properties = handleModification(event);
            final EntityDataChange e = new EntityDataChangeImpl(key, entity, properties);
            listener.preUpdate(e);
        }
    }
}