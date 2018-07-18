package com.ethlo.dachs.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.test.repository.CallRepository;
import com.ethlo.dachs.test.repository.CustomerRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@Sql(value="classpath:init.sql", executionPhase=ExecutionPhase.BEFORE_TEST_METHOD)
public class AbstractDataRepositoryTest
{
	@PersistenceContext
	protected EntityManager em;
	
	@Autowired
	protected PlatformTransactionManager txnManager;
	
	@Autowired
	protected CustomerRepository customerRepository;
	
	@Autowired
    protected CallRepository callRepository;

    protected TransactionTemplate txTpl;

	@Before
	public void txnTpl()
	{
	    this.txTpl = new TransactionTemplate(txnManager);
	}
	
	protected EntityDataChange getById(Collection<EntityDataChange> changes, Class<?> type, Serializable id)
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
		matches(change.getOldValue(), propType, oldValue);
		matches(change.getNewValue(), propType, newValue);
	}

    @SuppressWarnings("unchecked")
    private void matches(Object obj, Class<?> propType, Object expected)
    {
        if (Collection.class.isAssignableFrom(propType))
		{
		    final Collection<?> expectedNewValue = (Collection<?>) expected;
		    if (expectedNewValue == null)
		    {
		        assertThat(obj).isNull();
		    }
		    else if (expectedNewValue.isEmpty())
		    {
		        assertThat((Collection<?>)obj).isEmpty();
		    }
		    else
		    {
		        assertThat((Collection<Object>)obj).containsExactly(expectedNewValue.toArray());
		    }
		}
		else
		{
		    assertThat(obj).isEqualTo(expected);
		}
    }
}
