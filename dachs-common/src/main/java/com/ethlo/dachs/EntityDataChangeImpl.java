package com.ethlo.dachs;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public class EntityDataChangeImpl implements EntityDataChange
{
	private Serializable id;
	private final Object entity;
	private final Map<String, PropertyChange<?>> properties;
	
	public EntityDataChangeImpl(Serializable id, Object entity, Collection<PropertyChange<?>> properties)
	{
		this.id = id;
		this.entity = entity;
		this.properties = new TreeMap<>();
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
		if (obj instanceof EntityDataChangeImpl)
		{
			final EntityDataChangeImpl b = (EntityDataChangeImpl) obj;
			return Objects.equals(id, b.id) 
				&& Objects.equals(entity, b.entity)
				&& Objects.equals(properties, b.properties);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + ", entity=" + entity + ", properties=" + properties + "]";
	}

	public void setId(Serializable id)
	{
		this.id = id;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void prependIdPropertyChange(Field field, String idPropertyName, Object id, boolean deleted)
	{
	    if (field == null)
	    {
	        throw new IllegalArgumentException("field should not be null");
	    }
	    if (id == null)
        {
            throw new IllegalArgumentException("id should not be null");
        }
	    final PropertyChange propChange = new PropertyChange(idPropertyName, id.getClass(), deleted ? id : null, deleted ? null : id);
		this.properties.put(idPropertyName, propChange);
	}
}
