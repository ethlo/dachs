package com.ethlo.dachs.eclipselink;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import com.ethlo.dachs.util.ReflectionUtil;

public class PropertyAccessorCache
{
    private final Class<?> type;
    private final Map<String, Field> accessibleFields;

    public PropertyAccessorCache(final Class<?> type, final Map<String, Field> accessibleFields)
    {
        this.type = type;
        this.accessibleFields = Collections.unmodifiableMap(accessibleFields);
    }

    public Object getValue(String fieldName, Object target)
    {
        final Field field = accessibleFields.get(fieldName);
        if (field != null)
        {
            return ReflectionUtil.get(target, field);
        }
        return null;
    }

    public Map<String, Field> getFields()
    {
        return accessibleFields;
    }

    public Class<?> getType()
    {
        return type;
    }
}
