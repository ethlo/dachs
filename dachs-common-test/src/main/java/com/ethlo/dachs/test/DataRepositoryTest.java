package com.ethlo.dachs.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ethlo.dachs.CollectingEntityChangeSetListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.PropertyChange;

@RunWith(SpringJUnit4ClassRunner.class)
public class DataRepositoryTest
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private PlatformTransactionManager txnManager;
	
	@Autowired
	private CustomerRepository repository;
	
	@Autowired
	private CollectingEntityChangeSetListener listener;

	@Test
	public void testCreate()
	{
		final TransactionTemplate txTpl = new TransactionTemplate(txnManager);
		
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				// Create
				repository.save(new Customer("Jack", "Bauer"));
				repository.save(new Customer("Chloe", "O'Brian"));
				repository.save(new Customer("Kim", "Bauer"));
			}
		});
		
		final List<EntityDataChange> created = listener.getPostDataChangeSet().getCreated();
		Assert.assertEquals(3, created.size());
		
		final EntityDataChange created1 = getById(created, 1L);
		assertThat(created1.getId()).isEqualTo(1L);
		assertThat(created1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChanges1 = created1.getPropertyChanges();
		assertThat(createChanges1.size()).isEqualTo(4);
		assertMatch(createChanges1.get(0), "firstName", String.class, null, "Jack");
		assertMatch(createChanges1.get(1), "id", Long.class, null, 1L);
		assertMatch(createChanges1.get(2), "lastName", String.class, null, "Bauer");
		assertMatch(createChanges1.get(3), "tags", Set.class, null, new HashSet<>());
		
		txTpl.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				// Create
				repository.save(new Customer("Joe", "Cocker"));
				repository.save(new Customer("Michael", "Jackson"));
			}
		});
		
		final List<EntityDataChange> createdM = listener.getPostDataChangeSet().getCreated();
		Assert.assertEquals(2, createdM.size());
		
		final EntityDataChange createdM1 = getById(createdM, 4L);
		assertThat(createdM1.getId()).isEqualTo(4L);
		assertThat(createdM1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChangesM1 = createdM1.getPropertyChanges();
		assertThat(createChangesM1.size()).isEqualTo(4);
		assertMatch(createChangesM1.get(0), "firstName", String.class, null, "Joe");
		assertMatch(createChangesM1.get(1), "id", Long.class, null, 4L);
		assertMatch(createChangesM1.get(2), "lastName", String.class, null, "Cocker");
		assertMatch(createChangesM1.get(3), "tags", Set.class, null, new HashSet<>());
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
		
		final List<EntityDataChange> updated = listener.getPostDataChangeSet().getUpdated();
		Assert.assertEquals(1, updated.size());
		
		final EntityDataChange updated1 = getById(updated, 1L);
		assertThat(updated1.getId()).isEqualTo(1L);
		assertThat(updated1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> updateChanges1 = updated1.getPropertyChanges();
		assertThat(updateChanges1.size()).isEqualTo(2);
		assertMatch(updateChanges1.get(0), "firstName", String.class, "Jack", "Jack_updated");
		assertMatch(updateChanges1.get(1), "lastName", String.class, "Bauer", "Bauer_updated");
	}

	private EntityDataChange getById(List<EntityDataChange> changes, long id)
	{
		for (EntityDataChange e : changes)
		{
			if (Objects.equals(e.getId(), id))
			{
				return e;
			}
		}
		throw new IllegalArgumentException("Could not find change for entity id " + id);
	}

	private void assertMatch(@SuppressWarnings("rawtypes") PropertyChange change, String propName, Class<?> propType, Object oldValue, Object newValue)
	{
		assertThat(change.getPropertyName()).isEqualTo(propName);
		assertThat(change.getPropertyType().getCanonicalName()).isEqualTo(propType.getCanonicalName());
		assertThat(change.getOldValue()).isEqualTo(oldValue);
		assertThat(change.getNewValue()).isEqualTo(newValue);
	}
	
	@Ignore
	@Test
	public void performanceTest()
	{
		final int iterations = 20_000;
		for (int i = 0; i < iterations; i++)
		{
			repository.save(new Customer("Foo", "Bar " + i));
		}
		
		Assert.assertEquals(iterations, listener.getPostDataChangeSet().getCreated().size());
	}
}
