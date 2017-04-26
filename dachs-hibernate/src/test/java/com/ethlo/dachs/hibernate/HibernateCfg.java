package com.ethlo.dachs.hibernate;

import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

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
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
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
public class HibernateCfg extends JpaBaseConfiguration
{
    protected HibernateCfg(DataSource dataSource, JpaProperties properties, ObjectProvider<JtaTransactionManager> jtaTransactionManager,
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
		return new HibernateJpaVendorAdapter();
	}
	
	@Override
	protected Map<String, Object> getVendorProperties()
	{
		return Collections.singletonMap("hibernate.hbm2ddl.auto", "create");
	}
}
