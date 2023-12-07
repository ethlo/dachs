package com.ethlo.dachs;

/**
 * Similar to the {@link EntityChangeListener}, but grouping all changes that occurs within a unit of work, i.e a transaction.
 */
public interface EntityChangeSetListener
{
    /**
     * Triggered just before the transaction is about to commit
     * @param changeset The change set
     */
	default void preDataChanged(EntityDataChangeSet changeset)
    {

    }

	/**
     * Triggered just after the transaction is committed
     * @param changeset The change set
     */
	default void postDataChanged(EntityDataChangeSet changeset)
    {

    }
}