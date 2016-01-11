package com.ethlo.dachs;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EntityDataImpl implements EntityData
{
	private final Serializable id;
	private final Object entity;
	private final Map<String, PropertyChange<?>> properties;
	
	public EntityDataImpl(Serializable id, Object entity, Collection<PropertyChange<?>> properties)
	{
		this.id = id;
		this.entity = entity;
		this.properties = new LinkedHashMap<>();
		for (PropertyChange<?> propertyChange : properties)
		{
			this.properties.put(propertyChange.getPropertyName(), propertyChange);
		}
	}

	@Override
	public Serializable getId()
	{
		return id;
	}

	@Override
	public Object getEntity()
	{
		return entity;
	}

	@Override
	public Collection<PropertyChange<?>> getPropertyChanges()
	{
		return properties.values();
	}
	
	@Override
	public Optional<PropertyChange<?>> getPropertyChange(String propertyName)
	{
		return Optional.ofNullable(this.properties.get(propertyName));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof EntityDataImpl)
		{
			final EntityDataImpl b = (EntityDataImpl) obj;
			return Objects.equals(id, b.id) 
				&& Objects.equals(entity, b.entity)
				&& Objects.equals(properties, b.properties);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "EntityData [id=" + id + ", entity=" + entity + ", properties=" + properties + "]";
	}
}
