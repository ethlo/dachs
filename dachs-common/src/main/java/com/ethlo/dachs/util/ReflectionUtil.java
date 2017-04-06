package com.ethlo.dachs.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil
{
    public static Object get(Object someObject, String fieldName) throws RuntimeNoSuchFieldException
    {
        final Field field = getField(someObject.getClass(), fieldName);
        field.setAccessible(true);
        try
        {
            return field.get(someObject);
        }
        catch (IllegalAccessException exc)
        {
            throw new RuntimeIllegalAccessException(exc);
        }
    }
    
    public static List<Field> getAllFields(Class<?> type)
    {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass())
        {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
    
    public static Field getField(Class<?> type, String fieldName) throws RuntimeNoSuchFieldException
    {
        for (Class<?> c = type; c != null; c = c.getSuperclass())
        {
            try
            {
                return c.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException exc)
            {
                // Ignored
            }
            catch (SecurityException exc)
            {
                throw new RuntimeSecurityException(exc);
            }
        }
        throw new RuntimeNoSuchFieldException(new NoSuchFieldException(fieldName));
    }
}
