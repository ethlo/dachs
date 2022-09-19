package com.ethlo.dachs.jpa;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.util.Assert;

import com.ethlo.dachs.InternalEntityListener;

public class NotifyingJpaTransactionManager extends JpaTransactionManager
{
    private final InternalEntityListener listener;
    
    public NotifyingJpaTransactionManager(InternalEntityListener internalEntityListener)
    {
        Assert.notNull(internalEntityListener, "internalEntityListener cannot be null");
        this.listener = internalEntityListener;
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status)
    {
        listener.beforeCommit(status);
        super.doCommit(status);
        listener.afterCommit(status);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction)
    {
        super.doCleanupAfterCompletion(transaction);
        listener.cleanup(transaction);
    }

    @Override
    protected void doRollback(final DefaultTransactionStatus status)
    {
        super.doRollback(status);
        listener.rollback(status);
    }
}
