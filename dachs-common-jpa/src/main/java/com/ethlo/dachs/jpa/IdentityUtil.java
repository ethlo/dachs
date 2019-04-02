package com.ethlo.dachs.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceUnitUtil;

import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.EntityDataChangeImpl;
import com.ethlo.dachs.PropertyChange;

public class IdentityUtil
{
    private final PersistenceUnitUtil persistenceUnitUtil;
    
    public IdentityUtil(PersistenceUnitUtil persistenceUnitUtil)
    {
        this.persistenceUnitUtil = persistenceUnitUtil;
    }
    
    /**
     * Converts all the entities in the graph to simply contain their primary key. This is useful for logging and auditing as it 
     * avoids potential cycles in the graph.
     * 
     * @param list
     * @return
     */
    public Collection<EntityDataChange> toIndentityReferences(Collection<EntityDataChange> list)
    {
        return list.stream().map((entityData)->
                new EntityDataChangeImpl(entityData.getId(), entityData.getEntity(), entityData.getPropertyChanges().stream().map(this::transform).collect(Collectors.toList()))).collect(Collectors.toList());
    }    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> Object transform(Class<T> type, Object object)
    {
        if (Collection.class.isAssignableFrom(type))
        {
            return handleCollection((Collection<?>)object);
        }
        else if (Map.class.isAssignableFrom(type))
        {
            return handleMap((Map) object);
        }
        else if (isEntity(type))
        {
            return object != null ? getId(object) : null;
        }
        return object; 
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> PropertyChange<T> transform(PropertyChange<T> change)
    {
        final Class<?> type = change.getPropertyType();
        if (Collection.class.isAssignableFrom(type))
        {
            final Object oldRefColl = transform(type, change.getOldValue());
            final Object newRefColl = transform(type, change.getNewValue());
            return new PropertyChange(change.getPropertyName(), change.getPropertyType(), oldRefColl, newRefColl);
        }
        else if (Map.class.isAssignableFrom(type))
        {
            final Object oldRefColl = transform(type, change.getOldValue());
            final Object newRefColl = transform(type, change.getNewValue());
            return new PropertyChange(change.getPropertyName(), change.getPropertyType(), oldRefColl, newRefColl);
        }
        else if (isEntity(type))
        {
            final Serializable newId = change.getNewValue() != null ? getId(change.getNewValue()) : null;
            final Serializable oldId = change.getOldValue() != null ? getId(change.getOldValue()) : null;
            return new PropertyChange(change.getPropertyName(), change.getPropertyType(), oldId, newId);
        }
        return change; 
    }
    
    private Collection<?> handleCollection(Collection<?> coll)
    {
        if (coll == null)
        {
            return null;
        }
        final List<Object> tmp = new LinkedList<>();
        coll.forEach(e-> tmp.add(transform(e.getClass(), e)));
        return tmp;
    }
    
    private Map<?,?> handleMap(Map<Object,Object> map)
    {
        if (map == null)
        {
            return null;
        }
        final Map<Object, Object> tmp = new LinkedHashMap<>();
        map.forEach((key, value) -> tmp.put(key, transform(value.getClass(), value)));
        return tmp;
    }

    private boolean isEntity(Class<?> type)
    {
        return type.getAnnotation(Entity.class) != null 
           || type.getAnnotation(MappedSuperclass.class) != null;
    }
    
    private Serializable getId(Object entity)
    {
        return (Serializable) persistenceUnitUtil.getIdentifier(entity);
    }
}
