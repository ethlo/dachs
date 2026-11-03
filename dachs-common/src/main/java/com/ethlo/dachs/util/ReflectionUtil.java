package com.ethlo.dachs.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil
{
    public static Object get(Object someObject, String fieldName)
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

    public static Object get(Object someObject, Field field)
    {
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
        List<Field> fields = new ArrayList<>();
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

    /**
     * Read a property through its getter, falling back to the field. Getters let lazily loaded (woven) attributes load.
     */
    public static Object getPropertyValue(Object someObject, String propertyName)
    {
        final Method getter = getGetter(someObject.getClass(), propertyName);
        if (getter == null)
        {
            return get(someObject, propertyName);
        }
        try
        {
            return getter.invoke(someObject);
        }
        catch (IllegalAccessException exc)
        {
            throw new RuntimeIllegalAccessException(exc);
        }
        catch (InvocationTargetException exc)
        {
            final Throwable cause = exc.getCause();
            throw cause instanceof RuntimeException runtimeException ? runtimeException : new IllegalStateException(cause);
        }
    }

    private static Method getGetter(Class<?> type, String propertyName)
    {
        if (propertyName.isEmpty())
        {
            return null;
        }
        final String suffix = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (String name : new String[]{"get" + suffix, "is" + suffix})
        {
            try
            {
                final Method method = type.getMethod(name);
                if (method.getReturnType() != void.class && !Modifier.isStatic(method.getModifiers()) && method.trySetAccessible())
                {
                    return method;
                }
            }
            catch (NoSuchMethodException exc)
            {
                // Try next
            }
        }
        return null;
    }
}
