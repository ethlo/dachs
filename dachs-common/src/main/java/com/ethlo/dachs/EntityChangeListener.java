package com.ethlo.dachs;

/**
 * Basic interface for listening for entity changes as they are reported by the persistence framework.
 * Use {@link EntityChangeSetListener} if you need to listen for events as they are committed.
 *
 * @see EntityChangeSetListener
 * @see EntityChangeListenerAdapter
 */
public interface EntityChangeListener
{
    default void preCreate(EntityDataChange entityData)
    {

    }

    default void preUpdate(EntityDataChange entityData)
    {

    }

    default void preDelete(EntityDataChange entityData)
    {

    }

    default void created(EntityDataChange entityData)
    {

    }

    default void updated(EntityDataChange entityData)
    {

    }

    default void deleted(EntityDataChange entityData)
    {

    }

    default void rolledBackCreated(EntityDataChange e)
    {

    }

    default void rolledBackUpdated(EntityDataChange e)
    {

    }

    default void rolledBackDeleted(EntityDataChange e)
    {

    }
}