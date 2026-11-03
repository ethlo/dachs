package com.ethlo.dachs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.metamodel.Attribute;

@EntityListenerIgnore
public class MutableEntityDataChangeSet implements EntityDataChangeSet
{
    private final Collection<EntityDataChange> created;
    private final Collection<EntityDataChange> updated;
    private final Collection<EntityDataChange> deleted;

    public MutableEntityDataChangeSet()
    {
        this.created = new LinkedList<>();
        this.updated = new LinkedList<>();
        this.deleted = new LinkedList<>();
    }

    public static EntityDataChangeSet clone(EntityDataChangeSet cs)
    {
        final MutableEntityDataChangeSet e = new MutableEntityDataChangeSet();
        e.getCreated().addAll(clone(cs.getCreated()));
        e.getUpdated().addAll(clone(cs.getUpdated()));
        e.getDeleted().addAll(clone(cs.getDeleted()));
        return e;
    }

    private static Collection<? extends EntityDataChange> clone(Collection<EntityDataChange> l)
    {
        return l.stream().map(MutableEntityDataChangeSet::clone).collect(Collectors.toList());
    }

    private static EntityDataChange clone(EntityDataChange edc)
    {
        return new CopiedEntityDataChange(edc);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<PropertyChange<?>> clonePropChanges(Collection<PropertyChange<?>> propertyChanges)
    {
        final List<PropertyChange<?>> r = new ArrayList<>();
        propertyChanges.forEach(e -> r.add(new PropertyChange(e.getPropertyName(), e.getPropertyType(), e.getOldValue(), e.getNewValue())));
        return r;
    }

    @Override
    public Collection<EntityDataChange> getCreated()
    {
        return created;
    }

    @Override
    public Collection<EntityDataChange> getUpdated()
    {
        return updated;
    }

    @Override
    public Collection<EntityDataChange> getDeleted()
    {
        return deleted;
    }

    @Override
    public String toString()
    {
        return "ChangeSet [" + created.size() + " created=" + created + ", " + updated.size() + " updated=" + updated + ", " + deleted.size() + " deleted=" + deleted + "]";
    }

    @Override
    public boolean isEmpty()
    {
        return created.isEmpty() && updated.isEmpty() && deleted.isEmpty();
    }

    /**
     * A copy with its own {@link PropertyChange} instances that reads previous values of properties it has no change for
     * from the original. Typed lookups are passed to the original first, so its restrictions (such as those of
     * identity-converted changes) are kept, but are answered from the copy's own changes.
     */
    private static class CopiedEntityDataChange extends EntityDataChangeImpl
    {
        private final EntityDataChange original;

        CopiedEntityDataChange(EntityDataChange original)
        {
            super(original.getId(), original.getEntity(), clonePropChanges(original.getPropertyChanges()), original::getPreviousValue);
            this.original = original;
        }

        @Override
        public <T> Optional<PropertyChange<T>> getPropertyChange(Attribute<?, T> attribute)
        {
            original.getPropertyChange(attribute);
            return super.getPropertyChange(attribute);
        }

        @Override
        public <T> T getPreviousValue(Attribute<?, T> attribute)
        {
            original.getPropertyChange(attribute);
            return super.getPreviousValue(attribute);
        }
    }
}
