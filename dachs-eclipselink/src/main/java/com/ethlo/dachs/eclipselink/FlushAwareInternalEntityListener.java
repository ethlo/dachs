package com.ethlo.dachs.eclipselink;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

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
    public FlushAwareInternalEntityListener(
            final EntityManagerFactory emf,
            final Supplier<? extends Collection<EntityChangeSetListener>> setListenersSupplier,
            final Supplier<? extends Collection<EntityChangeListener>> listenersSupplier,
            final Supplier<? extends Collection<TransactionListener>> transactionListenersSupplier)
    {
        super(
                emf,
                setListenersSupplier,
                listenersSupplier,
                transactionListenersSupplier);
    }

    public FlushAwareInternalEntityListener(
            final EntityManagerFactory emf,
            final EntityChangeSetListener... setListeners)
    {
        super(emf, setListeners);
    }

    public FlushAwareInternalEntityListener(
            final EntityManagerFactory emf,
            final EntityChangeListener... listeners)
    {
        super(emf, listeners);
    }

    public FlushAwareInternalEntityListener(
            final EntityManagerFactory emf,
            final List<EntityChangeSetListener> listeners)
    {
        super(emf, listeners);
    }

    @Override
    protected boolean shouldFlush()
    {
        final EntityManager txnEm =
                EntityManagerFactoryUtils.getTransactionalEntityManager(getEmf());

        if (txnEm instanceof EntityManagerImpl)
        {
            final UnitOfWork iem =
                    ((EntityManagerImpl) txnEm).getUnitOfWork();

            if (iem instanceof RepeatableWriteUnitOfWork)
            {
                final boolean isWithinFlush =
                        ((RepeatableWriteUnitOfWork) iem).isWithinFlush();

                return !isWithinFlush;
            }
        }

        return getFlush();
    }
}