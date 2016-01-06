package com.ethlo.dachs.hibernate;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManagerFactory;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ethlo.dachs.CollectingEntityChangeSetListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.jpa.JpaTransactionManagerInterceptor;
import com.ethlo.dachs.test.Customer;
import com.ethlo.dachs.test.CustomerRepository;

@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses=CustomerRepository.class)
@EntityScan(basePackageClasses=Customer.class)
public class HibernateCfg extends JpaBaseConfiguration
{
	//@Autowired
	//private HibernatePropertyChangeInterceptor hibernatePropertyChangeInterceptor; 

	@Bean
	public HibernatePropertyChangeInterceptor interceptor(InternalEntityListener internalEntityListener)
	{
		return new HibernatePropertyChangeInterceptor(internalEntityListener);
	}
	
	@Bean
	public CollectingEntityChangeSetListener collectingListener()
	{
		return new CollectingEntityChangeSetListener();
	}
	
	@Override
	protected AbstractJpaVendorAdapter createJpaVendorAdapter()
	{
		return new HibernateJpaVendorAdapter();
	}
	
	@Bean
	public static JpaTransactionManagerInterceptor transactionManager(EntityManagerFactory emf, EntityChangeSetListener listener)
	{
		final JpaTransactionManagerInterceptor txnManager = new JpaTransactionManagerInterceptor(emf, listener);
		txnManager.setLazyIdExtractor(new HibernateLazyIdExtractor(emf));
		return txnManager;
	}
	
	@Bean
	public HibernatePropertyChangeInterceptorBridge bridge(HibernatePropertyChangeInterceptor interceptor)
	{
		HibernatePropertyChangeInterceptorBridge.setHibernatePropertyChangeInterceptor(interceptor);
		return new HibernatePropertyChangeInterceptorBridge();
	}

	@Override
	protected Map<String, Object> getVendorProperties()
	{
		final Map<String, Object> retVal = new TreeMap<>();
		
		// Connecting the listener to Dachs
		retVal.put("hibernate.ejb.interceptor", new HibernatePropertyChangeInterceptorBridge());
		retVal.put("hibernate.hbm2ddl.auto", "create");
		return retVal;
	}
}
