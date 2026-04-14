package com.ethlo.dachs.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.test.repository.CallRepository;
import com.ethlo.dachs.test.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Sql(value = "classpath:init.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class AbstractDataRepositoryTest
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

    @BeforeEach
    public void txnTpl()
    {
        this.txTpl = new TransactionTemplate(txnManager);
    }

    protected EntityDataChange getById(Collection<EntityDataChange> changes, Class<?> type, Serializable id)
    {
        // Modernized with Streams
        return changes.stream()
                .filter(e -> Objects.equals(e.getId(), id) && Objects.equals(e.getEntity().getClass(), type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find change for entity id " + id));
    }

    protected void assertMatch(PropertyChange<?> change, String propName, Class<?> propType, Object oldValue, Object newValue)
    {
        assertThat(change.getPropertyName()).isEqualTo(propName);
        assertThat(propType.isAssignableFrom(change.getPropertyType())).isTrue();
        matches(change.getOldValue(), propType, oldValue);
        matches(change.getNewValue(), propType, newValue);
    }

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
                assertThat((Collection<?>) obj).isEmpty();
            }
            else
            {
                assertThat((Collection) obj).containsExactlyElementsOf(expectedNewValue);
            }
        }
        else
        {
            assertThat(obj).isEqualTo(expected);
        }
    }
}