package com.ethlo.dachs.eclipselink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;
import java.util.List;

import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ethlo.dachs.CollectingEntityChangeSetListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.jpa.IdentityUtil;
import com.ethlo.dachs.test.AbstractDataRepositoryTest;
import com.ethlo.dachs.test.model.OrderLine;
import com.ethlo.dachs.test.model.Product;

@SpringBootTest(classes = EclipselinkCfg.class)
public class IdentityUtilTest extends AbstractDataRepositoryTest
{
    @Autowired
    private CollectingEntityChangeSetListener listener;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testPreviousValuesOfIdentityReferencesAreIds()
    {
        final Integer[] ids = txTpl.execute(t ->
        {
            final Product first = new Product("first", 100, Currency.getInstance("USD"));
            final Product second = new Product("second", 200, Currency.getInstance("USD"));
            final OrderLine line = new OrderLine().setProduct(first).setAmount(100).setCount(1);
            em.persist(second);
            em.persist(line);
            em.flush();
            return new Integer[]{line.getId(), first.getId(), second.getId()};
        });
        listener.clear();

        txTpl.execute(t ->
        {
            em.find(OrderLine.class, ids[0]).setCount(2);
            return null;
        });

        final EntityDataChange updated = getById(listener.getPostDataChangeSet().getUpdated(), OrderLine.class, ids[0]);
        final EntityDataChange identity = new IdentityUtil(entityManagerFactory.getPersistenceUnitUtil()).toIndentityReferences(List.of(updated)).iterator().next();

        assertThat(identity.getPreviousValue("count")).isEqualTo(1);
        assertThat(identity.getPreviousValue("product")).isEqualTo(ids[1]);
    }
}
