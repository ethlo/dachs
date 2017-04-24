package com.ethlo.dachs;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Allows the specification of a class and field filter for the change events
 */
public class EntityChangeSetListenerFilter implements EntityChangeSetListener
{
    private final EntityChangeSetListener delegate;
    private final Predicate<Object> classFilter;
    private final Predicate<PropertyChange<?>> fieldFilter;
    
    public EntityChangeSetListenerFilter(EntityChangeSetListener delegate, Predicate<Object> classFilter, Predicate<PropertyChange<?>> fieldFilter)
    {
        this.delegate = delegate;
        this.classFilter = classFilter;
        this.fieldFilter = fieldFilter;
    }

    @Override
    public void preDataChanged(EntityDataChangeSet changeset)
    {
        filter(changeset.getCreated());
        filter(changeset.getUpdated());
        filter(changeset.getDeleted());
        delegate.preDataChanged(changeset);
    }

    private void filter(Collection<EntityDataChange> changes)
    {
        final Iterator<EntityDataChange> changesIter = changes.iterator();
        while (changesIter.hasNext())
        {
            final EntityDataChange change = changesIter.next();
            if (classFilter != null && !classFilter.test(change.getEntity()))
            {
                changesIter.remove();
            }
            else
            {
                final Iterator<PropertyChange<?>> propChangeIter = change.getPropertyChanges().iterator();
                while (propChangeIter.hasNext())
                {
                    final PropertyChange<?> propChange = propChangeIter.next();
                    if (fieldFilter != null && !fieldFilter.test(propChange))
                    {
                        propChangeIter.remove();
                    }
                }
            }
        }
    }

    @Override
    public void postDataChanged(EntityDataChangeSet changeset)
    {
        filter(changeset.getCreated());
        filter(changeset.getUpdated());
        filter(changeset.getDeleted());
        delegate.postDataChanged(changeset);        
    }

    @Override
    public void begin()
    {
        // Empty implementation
    }
}