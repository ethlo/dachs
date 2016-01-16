package com.ethlo.dachs;

import java.util.LinkedList;
import java.util.List;

public class MutableEntityDataChangeSet implements EntityDataChangeSet
{
	private final List<EntityDataChange> created;
	private final List<EntityDataChange> updated;
	private final List<EntityDataChange> deleted;
	
	public MutableEntityDataChangeSet()
	{
		this.created = new LinkedList<>();
		this.updated = new LinkedList<>();
		this.deleted = new LinkedList<>();
	}

	@Override
	public List<EntityDataChange> getCreated()
	{
		return created;
	}

	@Override
	public List<EntityDataChange> getUpdated()
	{
		return updated;
	}

	@Override
	public List<EntityDataChange> getDeleted()
	{
		return deleted;
	}
}
