package com.ethlo.dachs;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PropertyChange<T>
{
    private Map<String, Annotation> annotations;
	private String propertyName;
	private Class<T> propertyType;
	private T oldValue;
	private T newValue;
	
	@SuppressWarnings("unused")
	private PropertyChange()
	{
		
	}
	
	public PropertyChange(String propertyName, Class<T> propertyType, T oldValue, T newValue, Annotation[] annotations)
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
		
		if (annotations != null)
		{
    		this.annotations = new HashMap<>(annotations.length);
    		for (Annotation ann : annotations)
    		{
    		    this.annotations.put(ann.annotationType().getCanonicalName(), ann);
    		}
		}
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

    public boolean hasAnnotation(Class<? extends Annotation> annotationType)
    {
        final String annName = annotationType.getCanonicalName();
        final boolean contains = annotations != null && annotations.get(annName) != null;
        return contains;
    }

    public Annotation[] getAnnotations()
    {
        return annotations != null ? annotations.values().toArray(new Annotation[annotations.size()]) : null; 
    }
}
