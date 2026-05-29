package com.ethlo.dachs.eclipselink.boot;

import java.lang.reflect.Modifier;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.function.SingletonSupplier;

import com.ethlo.dachs.EntityChangeListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.TransactionListener;
import com.ethlo.dachs.eclipselink.EclipseLinkEntityEventListener;
import com.ethlo.dachs.eclipselink.EclipseLinkToSpringContextBridge;
import com.ethlo.dachs.eclipselink.EclipselinkLazyIdExtractor;
import com.ethlo.dachs.eclipselink.FlushAwareInternalEntityListener;
import com.ethlo.dachs.jpa.DefaultInternalEntityListener;
import com.ethlo.dachs.jpa.NotifyingJpaTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;

@Configuration
@ConditionalOnProperty(
        name = "spring.jpa.properties.eclipselink.session.customizer",
        havingValue = "com.ethlo.dachs.eclipselink.DachsSessionCustomizer")
@AutoConfigureBefore(TransactionAutoConfiguration.class)
public class DachsEclipselinkAutoConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(DachsEclipselinkAutoConfiguration.class);

    @Value("${com.ethlo.dachs.eclipselink.explicit-flush:true}")
    private boolean forceFlush;

    @ConditionalOnMissingBean(value = NotifyingJpaTransactionManager.class)
    @Bean
    public static NotifyingJpaTransactionManager transactionManager(
            InternalEntityListener internalEntityListener)
    {
        return new NotifyingJpaTransactionManager(internalEntityListener);
    }

    @ConditionalOnMissingBean(value = InternalEntityListener.class)
    @Bean
    public InternalEntityListener internalEntityListener(
            EntityManagerFactory emf,
            ObjectProvider<EntityChangeSetListener> changeSetListenersProvider,
            ObjectProvider<EntityChangeListener> listenersProvider,
            ObjectProvider<TransactionListener> transactionListenersProvider)
    {
        final DefaultInternalEntityListener internalEntityListener =
                new FlushAwareInternalEntityListener(
                        emf,
                        SingletonSupplier.of(() -> resolveAndLog(changeSetListenersProvider, "EntityChangeSetListener")),
                        SingletonSupplier.of(() -> resolveAndLog(listenersProvider, "EntityChangeListener")),
                        SingletonSupplier.of(() -> resolveAndLog(transactionListenersProvider, "TransactionListener"))
                )
                        .setLazyIdExtractor(new EclipselinkLazyIdExtractor(emf))
                        .fieldFilter(field ->
                                !Modifier.isStatic(field.getModifiers())
                                        && field.getAnnotation(EntityListenerIgnore.class) == null
                                        && !field.getName().startsWith("_persistence_"));

        internalEntityListener.setFlush(forceFlush);

        final PersistenceUnitUtil persistenceUnitUtil =
                emf.getPersistenceUnitUtil();

        final EclipseLinkEntityEventListener handler =
                new EclipseLinkEntityEventListener(
                        persistenceUnitUtil,
                        internalEntityListener
                );

        EclipseLinkToSpringContextBridge.setEntityChangeListener(handler);

        return internalEntityListener;
    }

    /**
     * Resolves the beans from the ObjectProvider and logs the discovery exactly once.
     */
    private <T> List<T> resolveAndLog(ObjectProvider<T> provider, String listenerType)
    {
        final List<T> listeners = provider.orderedStream().toList();

        if (logger.isDebugEnabled())
        {
            final List<String> classNames = listeners.stream()
                    .map(l -> l.getClass().getName())
                    .toList();
            logger.debug("Dachs lazy-resolved {} {}s: {}", listeners.size(), listenerType, classNames);
        }

        return listeners;
    }
}