package com.ethlo.dachs;

import java.util.LinkedHashSet;
import java.util.Set;

@EntityListenerIgnore
public class MutableEntityDataChangeSet implements EntityDataChangeSet
{
	private final Set<EntityDataChange> created;
	private final Set<EntityDataChange> updated;
	private final Set<EntityDataChange> deleted;
	
	public MutableEntityDataChangeSet()
	{
		this.created = new LinkedHashSet<>();
		this.updated = new LinkedHashSet<>();
		this.deleted = new LinkedHashSet<>();
	}

	@Override
	public Set<EntityDataChange> getCreated()
	{
		return created;
	}

	@Override
	public Set<EntityDataChange> getUpdated()
	{
		return updated;
	}

	@Override
	public Set<EntityDataChange> getDeleted()
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
