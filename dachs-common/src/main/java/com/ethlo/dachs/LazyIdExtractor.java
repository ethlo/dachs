package com.ethlo.dachs;

import java.io.Serializable;

/**
 * Helper interface for supporting population of generated entity IDs after the event has been triggered.
 */
public interface LazyIdExtractor
{
	/**
	 * Return the ID of the specified entity
	 * @param entity The entity to get the ID from
	 * @return The ID of the entity
	 */
	Serializable extractId(Object entity);

	/**
	 * Return the ID property name of the entity
	 * @param entity The entity to get the id property name from
	 * @return the property name that is the identifier of the specified entity
	 */
	String extractIdPropertyName(Object entity);
}
