package com.ethlo.dachs;

/**
 * Empty implementation of {@link EntityChangeSetListener}
 */
public abstract class EntityChangeSetListenerAdapter implements EntityChangeSetListener
{
    @Override
    public void preDataChanged(EntityDataChangeSet changeset)
    {
        // Empty implementation
    }

    @Override
    public void postDataChanged(EntityDataChangeSet changeset)
    {
        // Empty implementation        
    }
}