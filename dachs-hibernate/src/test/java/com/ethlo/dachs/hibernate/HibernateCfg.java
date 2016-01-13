package com.ethlo.dachs.hibernate;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ethlo.dachs.EntityListener;
import com.ethlo.dachs.CollectingEntityListener;
import com.ethlo.dachs.test.Customer;
import com.ethlo.dachs.test.CustomerRepository;

@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses=CustomerRepository.class)
@EntityScan(basePackageClasses=Customer.class)
public class HibernateCfg extends JpaBaseConfiguration
{
	@Bean
	public CollectingEntityListener collectingListener()
	{
		return new CollectingEntityListener();
	}
	
	@Autowired
	private EntityListener listener;
	
	@Override
	protected AbstractJpaVendorAdapter createJpaVendorAdapter()
	{
		return new HibernateJpaVendorAdapter();
	}

	@Override
	protected Map<String, Object> getVendorProperties()
	{
		final Map<String, Object> retVal = new TreeMap<>();
		
		// Connecting the listener to Dachs
		final HibernatePropertyChangeInterceptor interceptor = new HibernatePropertyChangeInterceptor(new EntityListener[]{listener});
		//retVal.put("hibernate.ejb.interceptor", interceptor);
		
		retVal.put("hibernate.hbm2ddl.auto", "create");
		return retVal;
	}
}
