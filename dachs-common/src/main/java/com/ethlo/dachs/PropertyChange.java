package com.ethlo.dachs;

import java.util.Objects;

public class PropertyChange<T>
{
	private String propertyName;
	private Class<T> propertyType;
	private T oldValue;
	private T newValue;
	
	@SuppressWarnings("unused")
	private PropertyChange()
	{
		
	}
	
	public PropertyChange(String propertyName, Class<T> propertyType, T oldValue, T newValue)
	{
		if (propertyName == null)
		{
			throw new IllegalArgumentException("propertyName cannot be null");
		}
		this.propertyName = propertyName;
		if (propertyType == null)
		{
			throw new IllegalArgumentException("propertyType cannot be null");
		}
		this.propertyType = propertyType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public Class<T> getPropertyType()
	{
		return propertyType;
	}

	public T getOldValue()
	{
		return oldValue;
	}

	public T getNewValue()
	{
		return newValue;
	}

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
        result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
        result = prime * result + ((propertyType == null) ? 0 : propertyType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object b)
    {
        if (b instanceof PropertyChange)
        {
            final PropertyChange<?> pc = (PropertyChange<?>) b;
            return Objects.equals(propertyName, pc.propertyName)
                   && Objects.equals(newValue, pc.newValue)
                   && Objects.equals(oldValue, pc.oldValue)
                   && Objects.equals(propertyType, pc.propertyType);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "PropertyChange [propertyName=" + propertyName + ", entityType=" + propertyType + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
    }
}
