package com.ethlo.dachs;

import java.util.Collection;
import java.util.LinkedList;

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
