package com.ethlo.dachs.eclipselink;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fest.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ethlo.dachs.CollectingEntityListener;
import com.ethlo.dachs.EntityData;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.test.Customer;
import com.ethlo.dachs.test.CustomerRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EclipselinkCfg.class)
public class EclipselinkTest
{
	@Autowired
	private CustomerRepository repository;
	
	@Autowired
	private CollectingEntityListener listener;

	@Before
	public void reset()
	{
		listener.reset();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void smokeTest()
	{
		// Create
		repository.save(new Customer("Jack", "Bauer"));
		repository.save(new Customer("Chloe", "O'Brian"));
		repository.save(new Customer("Kim", "Bauer"));
		repository.save(new Customer("David", "Palmer"));
		Customer customer5 = repository.save(new Customer("Michelle", "Dessler"));
		
		// Update
		customer5.setFirstName("Dana");
		customer5.addTags("foo", "bar", "baz");
		customer5 = repository.save(customer5);
		
		// Delete
		repository.delete(customer5);
		
		Assert.assertEquals(5, listener.getCreated().size());
		final List<EntityData> created = Lists.newArrayList(listener.getCreated());
		final EntityData created1 = created.get(0);
		assertThat(created1.getId()).isEqualTo(1L);
		assertThat(created1.getEntity().getClass().getCanonicalName()).isEqualTo(Customer.class.getCanonicalName());
		final List<PropertyChange<?>> createChanges1 = Lists.newArrayList(created1.getPropertyChanges());
		assertThat(createChanges1.size()).isEqualTo(4);
		assertThat(createChanges1.get(0)).isEqualTo(new PropertyChange("id", Integer.class, null, 1));
		assertThat(createChanges1.get(1)).isEqualTo(new PropertyChange("firstName", String.class, null, "Jack"));
		assertThat(createChanges1.get(2)).isEqualTo(new PropertyChange("lastName", String.class, null, "Bauer"));
		assertThat(createChanges1.get(3)).isEqualTo(new PropertyChange("tags", Set.class, null, new HashSet<>()));
		
		Assert.assertEquals(1, listener.getUpdated().size());

		Assert.assertEquals(1, listener.getDeleted().size());
	}
	
	@Test
	public void performanceTest()
	{
		for (int i = 0; i < 20_000; i++)
		{
			repository.save(new Customer("Foo", "Bar"));
		}
		
		Assert.assertEquals(20_000, listener.getCreated().size());
	}

	@Ignore
	@Test
	public void performanceTestWithoutListener()
	{
		for (int i = 0; i < 20_000; i++)
		{
			repository.save(new Customer("Foo", "Bar"));
		}
		
		Assert.assertEquals(0, listener.getCreated().size());
	}
}
