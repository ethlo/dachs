package com.ethlo.dachs;

/**
 * Basic interface for listening for transaction events
 */
public interface TransactionListener
{
	void afterRollback(final Object txn);

	void afterCommit(final Object txn);

	void afterComplete(final Object txn);
}