package com.ethlo.dachs;

public interface EntityListener
{
	void preCreate(EntityDataChange entityData);

	void preUpdate(EntityDataChange entityData);
	
	void preDelete(EntityDataChange entityData);
	
	void created(EntityDataChange entityData);

	void updated(EntityDataChange entityData);
	
	void deleted(EntityDataChange entityData);
}