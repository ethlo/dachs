package com.ethlo.dachs;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Represents a single entity change which consists of one or more <code>{@link PropertyChange}</code>s.
 */
public interface EntityDataChange
{
	/**
	 * Returns the id of the entity
	 * @return the id of the entity
	 */
	Serializable getId();

	/**
	 * Returns the entity
	 * @return The entity
	 */
	Object getEntity();

	/**
	 * Get all propertyChanges
	 * @return A list of all property changes for this entity
	 */
	List<PropertyChange<?>> getPropertyChanges();

	/**
	 * Get a {@link PropertyChange} for the given propertyName of this entity
	 * @param propertyName The name of the entity property
	 * @return The {@link PropertyChange} for the given propertyName
	 */
	Optional<PropertyChange<?>> getPropertyChange(String propertyName);
}