package com.ethlo.dachs.eclipselink.boot;

import java.lang.reflect.Modifier;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

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
import com.ethlo.dachs.eclipselink.EclipseLinkEntityEventListener;
import com.ethlo.dachs.eclipselink.EclipseLinkToSpringContextBridge;
import com.ethlo.dachs.eclipselink.EclipselinkLazyIdExtractor;
import com.ethlo.dachs.jpa.DefaultInternalEntityListener;
import com.ethlo.dachs.jpa.NotifyingJpaTransactionManager;

@Configuration
@ConditionalOnProperty(name="spring.jpa.properties.eclipselink.session.customizer", havingValue="com.ethlo.dachs.eclipselink.DachsSessionCustomizer")
@AutoConfigureBefore(TransactionAutoConfiguration.class)
public class DachsEclipselinkAutoConfiguration
{
    @Autowired(required=false)
    private List<EntityChangeSetListener> txnListeners;
    
    @Autowired(required=false)
    private List<EntityChangeListener> listeners;
    
    @ConditionalOnMissingBean(value=InternalEntityListener.class)
    @Bean
    public InternalEntityListener internalEntityListener(EntityManagerFactory emf)
    {
        final DefaultInternalEntityListener internalEntityListener = new DefaultInternalEntityListener(emf, txnListeners, listeners)
            .setLazyIdExtractor(new EclipselinkLazyIdExtractor(emf))
            .fieldFilter(field->
            {
                return !Modifier.isStatic(field.getModifiers())
                    && field.getAnnotation(EntityListenerIgnore.class) == null
                    && !field.getName().startsWith("_persistence_");
            });
        
        final PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
        final EclipseLinkEntityEventListener handler = new EclipseLinkEntityEventListener(persistenceUnitUtil, internalEntityListener);
        EclipseLinkToSpringContextBridge.setEntityChangeListener(handler);
        return internalEntityListener;
    }
    
    @ConditionalOnMissingBean(value=NotifyingJpaTransactionManager.class)
    @Bean
    public static NotifyingJpaTransactionManager transactionManager(InternalEntityListener internalEntityListener)
    {
        return new NotifyingJpaTransactionManager(internalEntityListener);
    }   
}