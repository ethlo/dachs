package com.ethlo.dachs.hibernate;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.EntityManagerFactory;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import com.ethlo.dachs.AuditEntityListener;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.test.Customer;
import com.ethlo.dachs.test.CustomerRepository;

@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses=CustomerRepository.class)
@EntityScan(basePackageClasses=Customer.class)
public class HibernateCfg
{
	@Bean
	public Object setupDachs(EntityManagerFactory emf)
	{
		HibernateInjector.registerListeners(emf, new AuditEntityListener()
		{
			@Override
			public void update(Serializable key, Object entity, Collection<PropertyChange<?>> properties) {
				System.out.println("Updated "+ key + " - " + entity + " - " + StringUtils.collectionToCommaDelimitedString(properties));
			}
			
			@Override
			public void markUndeleted(Serializable key, Object entity) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void markDeleted(Serializable key, Object entity) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void delete(Serializable key, Object entity)
			{
				System.out.println("Deleted "+ key + " - " + entity);
			}
			
			@Override
			public void create(Serializable key, Object entity, Collection<PropertyChange<?>> properties) {
				System.out.println("Created "+ key + " - " + entity + " - " + StringUtils.collectionToCommaDelimitedString(properties));			}
		});
		System.out.println("Registered listener");
		return new Object();
	}
}
