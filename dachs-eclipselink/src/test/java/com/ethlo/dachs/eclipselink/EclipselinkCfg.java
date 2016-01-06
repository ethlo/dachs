package com.ethlo.dachs.eclipselink;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
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
public class EclipselinkCfg extends JpaBaseConfiguration
{
	@Bean
	public CollectingEntityChangeSetListener collectingListener()
	{
		return new CollectingEntityChangeSetListener();
	}
	
	@Override
	protected AbstractJpaVendorAdapter createJpaVendorAdapter()
	{
		return new EclipseLinkJpaVendorAdapter();
	}

	@Override
	protected Map<String, Object> getVendorProperties()
	{
		final Map<String, Object> retVal = new TreeMap<>();
		retVal.put(PersistenceUnitProperties.WEAVING, "static");
		retVal.put(PersistenceUnitProperties.DDL_GENERATION, "create-tables");
		retVal.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, DachsSessionCustomizer.class.getCanonicalName());
		return retVal;
	}
	
	@Bean
	public EclipseLinkToSpringContextBridge eclipseLinkToSpringContextBridge(EntityManagerFactory emf, InternalEntityListener internalEntityListener)
	{
		final PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
		final EclipseLinkAuditingLoggerHandler handler = new EclipseLinkAuditingLoggerHandler(persistenceUnitUtil, internalEntityListener);
		EclipseLinkToSpringContextBridge.setEntityChangeListener(handler);
		return new EclipseLinkToSpringContextBridge();
	}
	
	@Bean
	public static JpaTransactionManagerInterceptor transactionManager(EntityManagerFactory emf, EntityChangeSetListener listener)
	{
		return new JpaTransactionManagerInterceptor(emf, listener);
	}
}
