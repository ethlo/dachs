package com.ethlo.dachs;

public interface EntityListener
{
	void created(EntityData entityData);

	void updated(EntityData entityData);
	
	void deleted(EntityData entityData);
}