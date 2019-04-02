package com.ethlo.dachs.hibernate.boot;

import java.lang.reflect.Modifier;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ethlo.dachs.EntityChangeListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.TransactionListener;
import com.ethlo.dachs.hibernate.HibernateLazyIdExtractor;
import com.ethlo.dachs.hibernate.HibernatePropertyChangeInterceptor;
import com.ethlo.dachs.hibernate.HibernatePropertyChangeInterceptorBridge;
import com.ethlo.dachs.jpa.DefaultInternalEntityListener;
import com.ethlo.dachs.jpa.NotifyingJpaTransactionManager;

@Configuration
@ConditionalOnProperty(name="spring.jpa.properties.hibernate.ejb.interceptor", havingValue="com.ethlo.dachs.hibernate.HibernatePropertyChangeInterceptorBridge")
@AutoConfigureBefore(TransactionAutoConfiguration.class)
public class DachsHibernateAutoConfiguration
{
    @Autowired(required=false)
    private List<EntityChangeSetListener> changeSetListeners;
    
    @Autowired(required=false)
    private List<EntityChangeListener> listeners;

    @Autowired(required=false)
    private List<TransactionListener> transactionListeners;
    
    @ConditionalOnMissingBean(value=InternalEntityListener.class)
    @Bean
    public InternalEntityListener internalEntityListener(EntityManagerFactory emf)
    {
        final DefaultInternalEntityListener internalEntityListener = new DefaultInternalEntityListener(emf, changeSetListeners, listeners, transactionListeners)
            .setLazyIdExtractor(new HibernateLazyIdExtractor(emf))
            .fieldFilter(field->
                    !Modifier.isStatic(field.getModifiers())
                        && field.getAnnotation(EntityListenerIgnore.class) == null);
        
        HibernatePropertyChangeInterceptorBridge.setHibernatePropertyChangeInterceptor(new HibernatePropertyChangeInterceptor(internalEntityListener));

        return internalEntityListener;
    }
    
    @ConditionalOnMissingBean(value=NotifyingJpaTransactionManager.class)
    @Bean
    public static NotifyingJpaTransactionManager transactionManager(InternalEntityListener internalEntityListener)
    {
        return new NotifyingJpaTransactionManager(internalEntityListener);
    }   
}