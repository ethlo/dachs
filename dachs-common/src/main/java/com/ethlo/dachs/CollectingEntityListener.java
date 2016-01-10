package com.ethlo.dachs;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simple listener that collects all events so they can be introspected later. Mostly useful for testing purposes. 
 */
public class CollectingEntityListener implements EntityListener
{
	private final Collection<EntityData> created = new ConcurrentLinkedQueue<>();
	private final Collection<EntityData> updated = new ConcurrentLinkedQueue<>();
	private final Collection<EntityData> deleted = new ConcurrentLinkedQueue<>();
	
	
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
	
	public Collection<EntityData> getCreated()
	{
		return created;
	}

	public Collection<EntityData> getUpdated()
	{
		return updated;
	}

	public Collection<EntityData> getDeleted()
	{
		return deleted;
	}

	public void reset()
	{
		this.created.clear();
		this.updated.clear();
		this.deleted.clear();
	}
}