package com.ethlo.dachs.eclipselink;

import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.ethlo.dachs.CollectingEntityChangeListener;
import com.ethlo.dachs.CollectingEntityChangeSetListener;
import com.ethlo.dachs.test.model.Customer;
import com.ethlo.dachs.test.repository.CustomerRepository;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses=CustomerRepository.class)
@EntityScan(basePackageClasses=Customer.class)
public class EclipselinkCfg extends JpaBaseConfiguration
{
    protected EclipselinkCfg(DataSource dataSource, JpaProperties properties, ObjectProvider<JtaTransactionManager> jtaTransactionManager,
                    ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers)
    {
        super(dataSource, properties, jtaTransactionManager, transactionManagerCustomizers);
    }

    @Bean
	public CollectingEntityChangeSetListener collectingSetListener()
	{
		return new CollectingEntityChangeSetListener();
	}
	
	@Bean
	public CollectingEntityChangeListener collectingListener()
	{
		return new CollectingEntityChangeListener();
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
		return retVal;
	}
}
