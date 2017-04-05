package com.ethlo.dachs.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ethlo.dachs.CollectingEntityChangeSetListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.PropertyChange;

public class TransactionalDataRepositoryTest extends AbstractDataRepositoryTest
{
	@Autowired
	protected CollectingEntityChangeSetListener listener;
	
	@Before
	public void clear()
	{
		listener.clear();
	}
	
	@Test
	@DirtiesContext
	public void testCreate()
	{
		final TransactionTemplate txTpl = new TransactionTemplate(txnManager);

		final ProductOrder order = new ProductOrder();
		
		final AtomicLong firstId = new AtomicLong();
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				// Create
				final Customer c = new Customer("Jack", "Bauer");
				final Product p = new Product("C4", 1_000, Currency.getInstance("USD"));
				order.addLine(new OrderLine().withProduct(p));
				c.addOrder(order);
				
				final Customer first = repository.save(c);
				em.flush();
				firstId.set(first.getId());
				
				repository.save(new Customer("Chloe", "O'Brian"));
				repository.save(new Customer("Kim", "Bauer"));
			}
		});
		
		final Collection<EntityDataChange> created = listener.getPostDataChangeSet().getCreated();
		Assert.assertEquals(6, created.size());
		final EntityDataChange created1 = getById(created, Customer.class, firstId.get());
		assertThat(created1.getId()).isEqualTo(firstId.get());
		assertThat(created1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChanges1 = created1.getPropertyChanges();
		assertThat(createChanges1.size()).isEqualTo(5);
		assertMatch(createChanges1.get(0), "firstName", String.class, null, "Jack");
		assertMatch(createChanges1.get(1), "id", Long.class, null, firstId.get());
		assertMatch(createChanges1.get(2), "lastName", String.class, null, "Bauer");
		assertMatch(createChanges1.get(3), "orders", Collection.class, null, new ArrayList<>(Arrays.asList(order)));
		assertMatch(createChanges1.get(4), "tags", Set.class, null, new LinkedList<>());
		
		final AtomicLong joeId = new AtomicLong();
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				// Create
				final Customer joe = repository.save(new Customer("Joe", "Cocker"));
				em.flush();
				joeId.set(joe.getId());
				
				repository.save(new Customer("Michael", "Jackson"));
			}
		});
		
		final Collection<EntityDataChange> createdM = listener.getPostDataChangeSet().getCreated();
		Assert.assertEquals(2, createdM.size());
		
		final EntityDataChange createdM1 = getById(createdM, Customer.class, joeId.get());
		assertThat(createdM1.getId()).isEqualTo(joeId.get());
		assertThat(createdM1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChangesM1 = createdM1.getPropertyChanges();
		assertThat(createChangesM1.size()).isEqualTo(5);
		assertMatch(createChangesM1.get(0), "firstName", String.class, null, "Joe");
		assertMatch(createChangesM1.get(1), "id", Long.class, null, 5L);
		assertMatch(createChangesM1.get(2), "lastName", String.class, null, "Cocker");
		assertMatch(createChanges1.get(3), "orders", Collection.class, null, new ArrayList<>(Arrays.asList(order)));
		assertMatch(createChanges1.get(4), "tags", Set.class, null, new LinkedList<>());
	}
	
	@Test
	public void testUpdateNoChanges()
	{
		final TransactionTemplate txTpl = new TransactionTemplate(txnManager);
		
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				// Just load an entity, do not perform any changes
				repository.findOne(1L);
			}
		});
	}		
	
	@Test
	public void testUpdate()
	{
		final TransactionTemplate txTpl = new TransactionTemplate(txnManager);
		
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				final Customer existing1 = repository.findOne(1L);
				existing1.setFirstName(existing1.getFirstName() + "_updated");
				existing1.setLastName(existing1.getLastName() + "_updated");
			}
		});
		
		final Collection<EntityDataChange> updated = listener.getPostDataChangeSet().getUpdated();
		Assert.assertEquals(1, updated.size());
		
		final EntityDataChange updated1 = getById(updated, Customer.class, 1L);
		assertThat(updated1.getId()).isEqualTo(1L);
		assertThat(updated1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> updateChanges1 = updated1.getPropertyChanges();
		assertThat(updateChanges1.size()).isEqualTo(2);
		assertMatch(updateChanges1.get(0), "firstName", String.class, "Hugh", "Hugh_updated");
		assertMatch(updateChanges1.get(1), "lastName", String.class, "Jackman", "Jackman_updated");
	}
	
	@Test
	public void testDelete()
	{
		final TransactionTemplate txTpl = new TransactionTemplate(txnManager);
		
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				final Customer existing1 = repository.findOne(1L);
				repository.delete(existing1);
			}
		});
		
		final Collection<EntityDataChange> deleted = listener.getPostDataChangeSet().getDeleted();
		Assert.assertEquals(1, deleted.size());
		
		final EntityDataChange deleted1 = getById(deleted, Customer.class, 1L);
		assertThat(deleted1.getId()).isEqualTo(1L);
		assertThat(deleted1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> deleteChanges1 = deleted1.getPropertyChanges();
		assertThat(deleteChanges1.size()).isEqualTo(5);
		assertMatch(deleteChanges1.get(0), "firstName", String.class, "Hugh", null);
		assertMatch(deleteChanges1.get(1), "id", Long.class, 1L, null);
		assertMatch(deleteChanges1.get(2), "lastName", String.class, "Jackman", null);
		assertMatch(deleteChanges1.get(3), "orders", Collection.class, new ArrayList<>(), null);
		assertMatch(deleteChanges1.get(4), "tags", Set.class, new LinkedList<>(), null);
	}
	
	@Test
	public void performanceTest()
	{
		final int iterations = 100;
		final TransactionTemplate txTpl = new TransactionTemplate(txnManager);
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				for (int i = 0; i < iterations; i++)
				{
					repository.save(new Customer("Foo", "Bar " + i));
				}
			}
		});
		Assert.assertEquals(iterations, listener.getPostDataChangeSet().getCreated().size());
	}
}
