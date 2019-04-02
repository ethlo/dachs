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
    void preCreate(EntityDataChange entityData);

    void preUpdate(EntityDataChange entityData);

    void preDelete(EntityDataChange entityData);

    void created(EntityDataChange entityData);

    void updated(EntityDataChange entityData);

    void deleted(EntityDataChange entityData);

    void rolledBackCreated(EntityDataChange e);

    void rolledBackUpdated(EntityDataChange e);

    void rolledBackDeleted(EntityDataChange e);
}