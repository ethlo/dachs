package com.ethlo.dachs;

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
	public boolean equals(Object b)
	{
	    if (b instanceof PropertyChange)
	    {
	        return propertyName.equals(((PropertyChange<?>) b).propertyName);
	    }
	    return false;
	}
	
	@Override
	public int hashCode()
	{
	    return this.propertyName.hashCode();
	}
	
    @Override
    public String toString()
    {
        return "PropertyChange [propertyName=" + propertyName + ", entityType=" + propertyType + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
    }
}
