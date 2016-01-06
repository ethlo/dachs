package com.ethlo.dachs;

import java.util.List;

/**
 * Holder for all data changes performed in a unit of work, like a transaction 
 */
public interface EntityDataChangeSet
{
	/**
	 * Returns a list of all entities created
	 * @return a list of all entities created
	 */
	List<EntityDataChange> getCreated();

	/**
	 * Returns a list of all entities updated
	 * @return a list of all entities updated
	 */
	List<EntityDataChange> getUpdated();

	/**
	 * Returns a list of all entities deleted
	 * @return a list of all entities deleted
	 */
	List<EntityDataChange> getDeleted();
}