package com.ethlo.dachs.impl;

public interface EntityEventListener<E>
{
	void postPersistEvent(E event);
	
	void postRemoveEvent(E event);
	
	<T> void postUpdateEvent(E event);
}
