package com.ethlo.dachs;

/**
 * Listener used for bridging events from the persistence framework to Dachs
 */
public interface InternalEntityListener
{
	void preCreate(EntityDataChange entityData);

	void preUpdate(EntityDataChange entityData);
	
	void preDelete(EntityDataChange entityData);
	
	void created(EntityDataChange entityData);

	void updated(EntityDataChange entityData);
	
	void deleted(EntityDataChange entityData);
}