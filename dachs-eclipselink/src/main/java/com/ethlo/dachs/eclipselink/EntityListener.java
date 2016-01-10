package com.ethlo.dachs.eclipselink;

public interface EntityListener
{
	/**
	 * Executed after the entity manager remove operation is actually executed or cascaded. This call is synchronous with the remove operation.
	 * @param obj
	 */
	void postRemove(Object obj);

	/**
	 * Executed after the database UPDATE operation.
	 * @param obj
	 */
	void postUpdate(Object obj);
	
	/**
	 * Executed after the entity manager persist operation is actually executed or cascaded. This call is invoked after the database INSERT is executed.
	 * @param obj
	 */
	void postPersist(Object obj);
	
	/**
	 * Executed before the entity manager persist operation is actually executed or cascaded. This call is synchronous with the persist operation.
	 * @param obj
	 */
	void prePersist(Object obj);
}
