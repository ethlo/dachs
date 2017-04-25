package com.ethlo.dachs;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.ethlo.dachs.util.ReflectionUtil;

/**
 * Allows the specification of an entity and field filter for the change events
 */
public class DefaultFilteredEntityChangeSetListener implements FilteredEntityChangeSetListener
{
    private final EntityChangeSetListener delegate;
    private final Predicate<Object> entityFilter;
    private final Predicate<Map.Entry<Field, PropertyChange<?>>> fieldFilter;
    
    public DefaultFilteredEntityChangeSetListener(EntityChangeSetListener delegate, Predicate<Object> entityFilter, Predicate<Entry<Field, PropertyChange<?>>> fieldFilter)
    {
        this.delegate = delegate;
        this.entityFilter = entityFilter;
        this.fieldFilter = fieldFilter;
    }

    private void filter(EntityDataChangeSet changeset)
    {
        filter(changeset.getCreated());
        filter(changeset.getUpdated());
        filter(changeset.getDeleted());
    }

    private void filter(Collection<EntityDataChange> changes)
    {
        final Iterator<EntityDataChange> changesIter = changes.iterator();
        while (changesIter.hasNext())
        {
            final EntityDataChange change = changesIter.next();
            final Object entity = change.getEntity();
            if (entityFilter != null && !entityFilter.test(entity))
            {
                changesIter.remove();
            }
            else
            {
                final Iterator<PropertyChange<?>> propChangeIter = change.getPropertyChanges().iterator();
                while (propChangeIter.hasNext())
                { 
                    final PropertyChange<?> propChange = propChangeIter.next();
                    final String propName = propChange.getPropertyName();
                    final Field field = ReflectionUtil.getField(entity.getClass(), propName);
                    if (fieldFilter != null && !fieldFilter.test(new AbstractMap.SimpleEntry<>(field, propChange)))
                    {
                        propChangeIter.remove();
                    }
                }
            }
        }
    }

    @Override
    public Predicate<Entry<Field, PropertyChange<?>>> fieldFilter()
    {
        return this.fieldFilter;
    }

    @Override
    public Predicate<Object> entityFilter()
    {
        return entityFilter;
    }

    public void preDataChanged(EntityDataChangeSet changeset)
    {
        filter(changeset);
        delegate.preDataChanged(changeset);
    }

    public void postDataChanged(EntityDataChangeSet changeset)
    {
        filter(changeset);
        delegate.postDataChanged(changeset);
    }
}