package com.ethlo.dachs;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import com.ethlo.dachs.util.ReflectionUtil;
import jakarta.persistence.metamodel.Attribute;

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
	Collection<PropertyChange<?>> getPropertyChanges();

	/**
	 * Get a {@link PropertyChange} for the given propertyName of this entity
	 * @param propertyName The name of the entity property
	 * @return The {@link PropertyChange} for the given propertyName
	 */
	Optional<PropertyChange<?>> getPropertyChange(String propertyName);

	/**
	 * Get the {@link PropertyChange} for the given attribute of this entity
	 * @param attribute The metamodel attribute of the entity property
	 * @param <T> The type of the property
	 * @return The {@link PropertyChange} for the given attribute, or empty if the property did not change
	 */
	@SuppressWarnings("unchecked")
	default <T> Optional<PropertyChange<T>> getPropertyChange(Attribute<?, T> attribute)
	{
		return getPropertyChange(attribute.getName()).map(change -> (PropertyChange<T>) change);
	}

	/**
	 * Get the current value of the given property of the entity. Implementations backed by a persistence provider read it
	 * the way the provider does (respecting the access type and loading lazy attributes); the default reads the field.
	 * @param propertyName The name of the entity property
	 * @return The current value of the property
	 */
	default Object getCurrentValue(String propertyName)
	{
		return ReflectionUtil.get(getEntity(), propertyName);
	}

	/**
	 * Get the value the given property had before this change: the old value if the property changed,
	 * otherwise its current value (see {@link #getCurrentValue(String)})
	 * @param propertyName The name of the entity property
	 * @return The previous value of the property
	 */
	default Object getPreviousValue(String propertyName)
	{
		final Optional<PropertyChange<?>> change = getPropertyChange(propertyName);
		return change.isPresent() ? change.get().getOldValue() : getCurrentValue(propertyName);
	}

	/**
	 * Get the value the given attribute had before this change: the old value if the property changed,
	 * otherwise its current value (see {@link #getCurrentValue(String)})
	 * @param attribute The metamodel attribute of the entity property
	 * @param <T> The type of the property
	 * @return The previous value of the property
	 */
	@SuppressWarnings("unchecked")
	default <T> T getPreviousValue(Attribute<?, T> attribute)
	{
		return (T) getPreviousValue(attribute.getName());
	}
}
