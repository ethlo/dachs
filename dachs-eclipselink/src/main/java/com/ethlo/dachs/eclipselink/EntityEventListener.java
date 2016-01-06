package com.ethlo.dachs.eclipselink;

public interface EntityEventListener<E>
{
	void prePersistEvent(E event);
	
	void preRemoveEvent(E event);
	
	void preUpdateEvent(E event);
	
	void postPersistEvent(E event);
	
	void postRemoveEvent(E event);
	
	<T> void postUpdateEvent(E event);
}
