package com.ethlo.dachs;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Listener used for bridging events from the persistence framework implementation to Dachs
 */
public interface InternalEntityListener
{
	void preCreate(EntityDataChange entityData);

	void preUpdate(EntityDataChange entityData);
	
	void preDelete(EntityDataChange entityData);
	
	void created(EntityDataChange entityData);

	void updated(EntityDataChange entityData);
	
	void deleted(EntityDataChange entityData);
	
	Predicate<Field> getFieldFilter();
	
	Predicate<Object> getEntityFilter();

    void postFlush(Iterator<Object> entities);

    void begin();

    void beforeCommit();

    void afterCommit();
    
    
}