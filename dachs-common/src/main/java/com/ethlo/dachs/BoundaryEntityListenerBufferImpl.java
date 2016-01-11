package com.ethlo.dachs;

import java.util.LinkedList;
import java.util.List;

public class BoundaryEntityListenerBufferImpl implements BoundaryEntityListenerBuffer
{
	private final List<EntityData> created = new LinkedList<>();
	private final List<EntityData> updated = new LinkedList<>();
	private final List<EntityData> deleted = new LinkedList<>();
	
	@Override
	public void created(EntityData entityData)
	{
		this.created.add(entityData);
	}

	@Override
	public void updated(EntityData entityData)
	{
		this.updated.add(entityData);
	}

	@Override
	public void deleted(EntityData entityData)
	{
		this.deleted.add(entityData);
	}

	@Override
	public void start()
	{
		
	}

	@Override
	public void discard() {
		this.created.clear();
		this.updated.clear();
		this.deleted.clear();
	}

	@Override
	public void flush()
	{
		
	}

	@Override
	public Iterable<PropertyChange<?>> getPendingChanges()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
