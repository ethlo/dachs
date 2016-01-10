package com.ethlo.dachs.eclipselink;

public interface EntityEventListener<E>
{
	void postPersistEvent(E event);
	
	void postRemoveEvent(E event);
	
	<T> void postUpdateEvent(E event);
}
