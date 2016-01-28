package com.ethlo.dachs.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ethlo.dachs.CollectingEntityChangeListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.PropertyChange;

@RunWith(SpringJUnit4ClassRunner.class)
@Sql(value="classpath:init.sql", executionPhase=ExecutionPhase.BEFORE_TEST_METHOD)
public class DirectDataRepositoryTest
{
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private CustomerRepository repository;
	
	@Autowired
	private CollectingEntityChangeListener listener;
	
	@Before
	public void clear()
	{
		listener.clear();
	}

	@Test
	@DirtiesContext
	public void testCreate()
	{
		final AtomicLong firstId = new AtomicLong();

		// Create
		final Customer first = repository.save(new Customer("Jack", "Bauer"));
		firstId.set(first.getId());
		
		repository.save(new Customer("Chloe", "O'Brian"));
		repository.save(new Customer("Kim", "Bauer"));
		
		final List<EntityDataChange> created = listener.getPostCreated();
		Assert.assertEquals(3, created.size());
		final EntityDataChange created1 = getById(created, firstId.get());
		assertThat(created1.getId()).isEqualTo(firstId.get());
		assertThat(created1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChanges1 = created1.getPropertyChanges();
		assertThat(createChanges1.size()).isEqualTo(4);
		assertMatch(createChanges1.get(0), "firstName", String.class, null, "Jack");
		assertMatch(createChanges1.get(1), "id", Long.class, null, firstId.get());
		assertMatch(createChanges1.get(2), "lastName", String.class, null, "Bauer");
		assertMatch(createChanges1.get(3), "tags", Set.class, null, new HashSet<>());
		
		final AtomicLong joeId = new AtomicLong();
		final Customer joe = repository.save(new Customer("Joe", "Cocker"));
		joeId.set(joe.getId());
		
		repository.save(new Customer("Michael", "Jackson"));
		
		final List<EntityDataChange> createdM = listener.getPostCreated();
		Assert.assertEquals(5, createdM.size());
		
		final EntityDataChange createdM1 = getById(createdM, joeId.get());
		assertThat(createdM1.getId()).isEqualTo(joeId.get());
		assertThat(createdM1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChangesM1 = createdM1.getPropertyChanges();
		assertThat(createChangesM1.size()).isEqualTo(4);
		assertMatch(createChangesM1.get(0), "firstName", String.class, null, "Joe");
		assertMatch(createChangesM1.get(1), "id", Long.class, null, 5L);
		assertMatch(createChangesM1.get(2), "lastName", String.class, null, "Cocker");
		assertMatch(createChangesM1.get(3), "tags", Set.class, null, new HashSet<>());
	}
	
	@Test
	public void testUpdateNoChanges()
	{
		repository.findOne(1L);
	}		
	
	@Test
	public void testUpdate()
	{
		final Customer existing1 = repository.findOne(1L);
		existing1.setFirstName(existing1.getFirstName() + "_updated");
		existing1.setLastName(existing1.getLastName() + "_updated");
		repository.save(existing1);
		
		final List<EntityDataChange> updated = listener.getPostUpdated();
		Assert.assertEquals(1, updated.size());
		
		final EntityDataChange updated1 = getById(updated, 1L);
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
		final Customer existing1 = repository.findOne(1L);
		repository.delete(existing1);
		
		final List<EntityDataChange> deleted = listener.getPostDeleted();
		Assert.assertEquals(1, deleted.size());
		
		final EntityDataChange deleted1 = getById(deleted, 1L);
		assertThat(deleted1.getId()).isEqualTo(1L);
		assertThat(deleted1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> deleteChanges1 = deleted1.getPropertyChanges();
		assertThat(deleteChanges1.size()).isEqualTo(4);
		assertMatch(deleteChanges1.get(0), "firstName", String.class, "Hugh", null);
		assertMatch(deleteChanges1.get(1), "id", Long.class, 1L, null);
		assertMatch(deleteChanges1.get(2), "lastName", String.class, "Jackman", null);
		assertMatch(deleteChanges1.get(3), "tags", Set.class, new LinkedHashSet<>(), null);
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
	
	@Test
	public void performanceTest()
	{
		final int iterations = 1_000;
		for (int i = 0; i < iterations; i++)
		{
			repository.save(new Customer("Foo", "Bar " + i));
		}
		Assert.assertEquals(iterations, listener.getPostCreated().size());
	}
}
