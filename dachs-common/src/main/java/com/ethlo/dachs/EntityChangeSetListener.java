package com.ethlo.dachs;

/**
 * Similar to the {@link EntityChangeListener}, but grouping all changes that occurs within a unit of work, i.e a transaction.
 */
public interface EntityChangeSetListener
{
	void preDataChanged(EntityDataChangeSet changeset);
	
	void postDataChanged(EntityDataChangeSet changeset);

	void begin();
}