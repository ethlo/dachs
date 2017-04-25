package com.ethlo.dachs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        return l.stream().map(i->clone(i)).collect(Collectors.toList());
    }

    private static EntityDataChange clone(EntityDataChange edc)
    {
        return new EntityDataChangeImpl(edc.getId(), edc.getEntity(), clonePropChanges(edc.getPropertyChanges()));
    }

    private static Collection<PropertyChange<?>> clonePropChanges(Collection<PropertyChange<?>> propertyChanges)
    {
        final List<PropertyChange<?>> r = new ArrayList<>();
        propertyChanges
            .stream()
            .forEach(e->r.add(e.copy()));
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
	public String toString() {
		return "ChangeSet [" + created.size() + " created=" + created + ", " + updated.size() + " updated=" + updated + ", " + deleted.size() + " deleted=" + deleted + "]";
	}

	@Override
	public boolean isEmpty()
	{
		return created.isEmpty() && updated.isEmpty() && deleted.isEmpty();
	}
}
