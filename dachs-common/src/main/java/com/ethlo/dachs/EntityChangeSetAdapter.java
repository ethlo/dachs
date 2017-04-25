package com.ethlo.dachs;

/**
 * Empty implementation of {@link EntityChangeSetListener}
 */
public class EntityChangeSetAdapter implements EntityChangeSetListener
{
	@Override
	public void preDataChanged(EntityDataChangeSet changeset) {}

	@Override
	public void postDataChanged(EntityDataChangeSet changeset) {}
}