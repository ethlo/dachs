package com.ethlo.dachs;

import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 *  
 */
public interface FilteredEntityChangeSetListener extends EntityChangeSetListener
{
    Predicate<Entry<Field, PropertyChange<?>>> fieldFilter();
    Predicate<Object> entityFilter();
}