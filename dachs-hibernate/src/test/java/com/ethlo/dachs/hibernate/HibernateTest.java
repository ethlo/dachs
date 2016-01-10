package com.ethlo.dachs.hibernate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ethlo.dachs.CollectingEntityListener;
import com.ethlo.dachs.test.Customer;
import com.ethlo.dachs.test.CustomerRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HibernateCfg.class)
public class HibernateTest
{
	@Autowired
	private CustomerRepository repository;
	
	@Autowired
	private CollectingEntityListener listener;
	
	@Test
	public void smokeTest()
	{
		// Create
		repository.save(new Customer("Jack", "Bauer"));
		repository.save(new Customer("Chloe", "O'Brian"));
		repository.save(new Customer("Kim", "Bauer"));
		repository.save(new Customer("David", "Palmer"));
		final Customer customer5 = repository.save(new Customer("Michelle", "Dessler"));
		
		// Update
		customer5.setFirstName("Dana");
		repository.save(customer5);
		
		// Delete
		repository.delete(customer5);
		
		Assert.assertEquals(5, listener.getCreated().size());
		
		Assert.assertEquals(1, listener.getUpdated().size());

		Assert.assertEquals(1, listener.getDeleted().size());
	}
}
