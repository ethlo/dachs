package com.ethlo.dachs;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple listener that collects all events so they can be inspected later. Mostly useful for testing purposes. 
 */
public class CollectingEntityChangeListener implements EntityChangeListener
{
	private List<EntityDataChange> preCreated = new LinkedList<>();
	private List<EntityDataChange> postCreated = new LinkedList<>();
	private List<EntityDataChange> preUpdated = new LinkedList<>();
	private List<EntityDataChange> postUpdated = new LinkedList<>();
	private List<EntityDataChange> preDeleted = new LinkedList<>();
	private List<EntityDataChange> postDeleted = new LinkedList<>();
	private List<EntityDataChange> rollbackCreated = new LinkedList<>();
	private List<EntityDataChange> rollbackUpdated = new LinkedList<>();
	private List<EntityDataChange> rollbackDeleted = new LinkedList<>();
	
	@Override
	public void preCreate(EntityDataChange entityData)
	{
		this.preCreated.add(entityData);
	}

	@Override
	public void preUpdate(EntityDataChange entityData)
	{
		this.preUpdated.add(entityData);
	}

	@Override
	public void preDelete(EntityDataChange entityData)
	{
		this.preDeleted.add(entityData);
	}

	@Override
	public void created(EntityDataChange entityData)
	{
		this.postCreated.add(entityData);		
	}

	@Override
	public void updated(EntityDataChange entityData)
	{
		this.postUpdated.add(entityData);
	}

	@Override
	public void deleted(EntityDataChange entityData)
	{
		this.postDeleted.add(entityData);
	}

	@Override
	public void rolledBackCreated(final EntityDataChange e)
	{
		rollbackCreated.add(e);
	}

	@Override
	public void rolledBackUpdated(final EntityDataChange e)
	{
		rollbackUpdated.add(e);
	}

	@Override
	public void rolledBackDeleted(final EntityDataChange e)
	{
		rolledBackDeleted(e);
	}

	public void clear()
	{
		this.preCreated.clear();
		this.preUpdated.clear();
		this.preDeleted.clear();
		this.postCreated.clear();
		this.postUpdated.clear();
		this.postDeleted.clear();
	}

	public List<EntityDataChange> getPreCreated()
	{
		return preCreated;
	}

	public List<EntityDataChange> getPostCreated()
	{
		return postCreated;
	}

	public List<EntityDataChange> getPreUpdated()
	{
		return preUpdated;
	}

	public List<EntityDataChange> getPostUpdated()
	{
		return postUpdated;
	}

	public List<EntityDataChange> getPreDeleted()
	{
		return preDeleted;
	}

	public List<EntityDataChange> getPostDeleted()
	{
		return postDeleted;
	}

	public List<EntityDataChange> getRollbackCreated()
	{
		return rollbackCreated;
	}

	public List<EntityDataChange> getRollbackUpdated()
	{
		return rollbackUpdated;
	}

	public List<EntityDataChange> getRollbackDeleted()
	{
		return rollbackDeleted;
	}
}