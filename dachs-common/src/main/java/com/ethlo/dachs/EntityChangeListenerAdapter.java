package com.ethlo.dachs;

/**
 * Empty implementation of {@link EntityChangeListener}
 */
public class EntityChangeListenerAdapter implements EntityChangeListener
{
	@Override
	public void created(EntityDataChange entityData)
	{
	}

	@Override
	public void updated(EntityDataChange entityData)
	{
	}

	@Override
	public void deleted(EntityDataChange entityData)
	{
	}

	@Override
	public void rolledBackCreated(final EntityDataChange e)
	{

	}

	@Override
	public void rolledBackUpdated(final EntityDataChange e)
	{

	}

	@Override
	public void rolledBackDeleted(final EntityDataChange e)
	{

	}

	@Override
	public void preCreate(EntityDataChange entityData)
	{
	}

	@Override
	public void preUpdate(EntityDataChange entityData)
	{
	}

	@Override
	public void preDelete(EntityDataChange entityData)
	{
	}
}
