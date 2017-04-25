package com.ethlo.dachs;

/**
 * Simple listener that collects all events so they can be inspected later. Mostly useful for testing purposes. 
 */
public class CollectingEntityChangeSetListener implements EntityChangeSetListener
{
	private EntityDataChangeSet preDataChangeSet;
	private EntityDataChangeSet postDataChangeSet;

	@Override
	public void preDataChanged(EntityDataChangeSet changeset)
	{
		this.preDataChangeSet = changeset;
	}

	@Override
	public void postDataChanged(EntityDataChangeSet changeset)
	{
		this.postDataChangeSet = changeset;
	}

	public EntityDataChangeSet getPreDataChangeSet()
	{
		return preDataChangeSet != null ? preDataChangeSet : new MutableEntityDataChangeSet();
	}

	public EntityDataChangeSet getPostDataChangeSet()
	{
		return postDataChangeSet != null ? postDataChangeSet : new MutableEntityDataChangeSet();
	}

	public void clear()
	{
	    this.preDataChangeSet = null;
        this.postDataChangeSet = null;
	}
}