package com.ethlo.dachs.jpa;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.util.Assert;

import com.ethlo.dachs.InternalEntityListener;

public class NotifyingJpaTransactionManager extends JpaTransactionManager
{
    private static final long serialVersionUID = 9113827563871441811L;
    private final InternalEntityListener listener;
    
    public NotifyingJpaTransactionManager(InternalEntityListener internalEntityListener)
    {
        Assert.notNull(internalEntityListener, "internalEntityListener cannot be null");
        this.listener = internalEntityListener;
    }
    
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition)
    {
        super.doBegin(transaction, definition);
        listener.begin();
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status)
    {
        listener.beforeCommit();
        super.doCommit(status);
        listener.afterCommit();
    }
}
