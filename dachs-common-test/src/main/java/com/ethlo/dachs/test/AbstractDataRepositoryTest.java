package com.ethlo.dachs.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.PropertyChange;

@RunWith(SpringJUnit4ClassRunner.class)
@Sql(value="classpath:init.sql", executionPhase=ExecutionPhase.BEFORE_TEST_METHOD)

public class AbstractDataRepositoryTest
{
	@PersistenceContext
	protected EntityManager em;
	
	@Autowired
	protected PlatformTransactionManager txnManager;
	
	@Autowired
	protected CustomerRepository repository;
	
	protected EntityDataChange getById(List<EntityDataChange> changes, Class<?> type, long id)
	{
		for (EntityDataChange e : changes)
		{
			if (Objects.equals(e.getId(), id) && Objects.equals(e.getEntity().getClass(), type))
			{
				return e;
			}
		}
		throw new IllegalArgumentException("Could not find change for entity id " + id);
	}

	protected void assertMatch(@SuppressWarnings("rawtypes") PropertyChange change, String propName, Class<?> propType, Object oldValue, Object newValue)
	{
		assertThat(change.getPropertyName()).isEqualTo(propName);
		assertThat(propType.isAssignableFrom(change.getPropertyType())).isTrue();
		assertThat(change.getOldValue()).isEqualTo(oldValue);
		assertThat(change.getNewValue()).isEqualTo(newValue);
	}
}
