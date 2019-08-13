package com.ethlo.dachs.eclipselink;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import com.ethlo.dachs.EntityChangeListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.TransactionListener;
import com.ethlo.dachs.jpa.DefaultInternalEntityListener;

public class FlushAwareInternalEntityListener extends DefaultInternalEntityListener
{
    public FlushAwareInternalEntityListener(final EntityManagerFactory emf, final Collection<EntityChangeSetListener> setListeners, final Collection<EntityChangeListener> listeners, final Collection<TransactionListener> transactionListeners)
    {
        super(emf, setListeners, listeners, transactionListeners);
    }

    public FlushAwareInternalEntityListener(final EntityManagerFactory emf, final EntityChangeSetListener... setListeners)
    {
        super(emf, setListeners);
    }

    public FlushAwareInternalEntityListener(final EntityManagerFactory emf, final EntityChangeListener... listeners)
    {
        super(emf, listeners);
    }

    public FlushAwareInternalEntityListener(final EntityManagerFactory emf, final List<EntityChangeSetListener> listeners)
    {
        super(emf, listeners);
    }

    @Override
    protected boolean shouldFlush()
    {
        final EntityManager txnEm = EntityManagerFactoryUtils.getTransactionalEntityManager(getEmf());
        if (txnEm instanceof EntityManagerImpl)
        {
            final UnitOfWork iem = ((EntityManagerImpl) txnEm).getUnitOfWork();
            if (iem instanceof RepeatableWriteUnitOfWork)
            {
                final boolean isWithinFlush = ((RepeatableWriteUnitOfWork) iem).isWithinFlush();
                return !isWithinFlush;
            }
        }
        return getFlush();
    }
}
