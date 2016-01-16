package com.ethlo.dachs;

public class PropertyChange<T>
{
	private String propertyName;
	private Class<T> entityType;
	private T oldValue;
	private T newValue;
	
	@SuppressWarnings("unused")
	private PropertyChange()
	{
		
	}
	
	public PropertyChange(String propertyName, Class<T> entityType, T oldValue, T newValue)
	{
		this.propertyName = propertyName;
		this.entityType = entityType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public Class<T> getPropertyType()
	{
		return entityType;
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
        return "PropertyChange [propertyName=" + propertyName + ", entityType=" + entityType + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
    }
}
