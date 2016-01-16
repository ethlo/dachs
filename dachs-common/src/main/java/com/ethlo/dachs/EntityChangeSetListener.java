package com.ethlo.dachs;

/**
 * Similar to the {@link EntityListener}, but grouping all changes, usually bound by a transaction.
 */
public interface EntityChangeSetListener
{
	void preDataChanged(EntityDataChangeSet changeset);
	
	void postDataChanged(EntityDataChangeSet changeset);

	void begin();
}
