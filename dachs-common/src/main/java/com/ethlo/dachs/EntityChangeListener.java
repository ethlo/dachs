package com.ethlo.dachs;

/**
 * Basic interface for listening for entity changes.
 * 
 * @see EntityChangeSetListener
 * @see EntityListenerAdapter
 */
public interface EntityChangeListener
{
	void preCreate(EntityDataChange entityData);

	void preUpdate(EntityDataChange entityData);
	
	void preDelete(EntityDataChange entityData);
	
	void created(EntityDataChange entityData);

	void updated(EntityDataChange entityData);
	
	void deleted(EntityDataChange entityData);
}