package com.ethlo.dachs;

import java.io.Serializable;
import java.util.Collection;

public interface AuditEntityListener
{
	void create(Serializable key, Object entity, Collection<PropertyChange<?>> properties);

	void update(Serializable key, Object entity, Collection<PropertyChange<?>> properties);
	
	void delete(Serializable key, Object entity);
	
	void markDeleted(Serializable key, Object entity);
	
	void markUndeleted(Serializable key, Object entity);
}