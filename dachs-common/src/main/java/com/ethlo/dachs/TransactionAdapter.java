package com.ethlo.dachs;

/**
 * Basic adapter for listening for transaction events
 */
public class TransactionAdapter implements TransactionListener
{
    @Override
    public void afterRollback(final Object txn)
    {

    }

    @Override
    public void afterCommit(final Object txn)
    {

    }

    @Override
    public void afterComplete(final Object txn)
    {

    }
}