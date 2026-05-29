package com.ethlo.dachs.jpa;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.EntityChangeListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.EntityDataChangeImpl;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.LazyIdExtractor;
import com.ethlo.dachs.MutableEntityDataChangeSet;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.TransactionListener;
import com.ethlo.dachs.util.ReflectionUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.IdClass;

/**
 * Caches entity changes until the transaction commits, or discards them in case of a roll-back.
 */
public class DefaultInternalEntityListener implements InternalEntityListener, Serializable
{
    private static final ThreadLocal<MutableEntityDataChangeSet> preChangeset =
            ThreadLocal.withInitial(MutableEntityDataChangeSet::new);
    private static final ThreadLocal<MutableEntityDataChangeSet> postChangeset =
            ThreadLocal.withInitial(MutableEntityDataChangeSet::new);
    private final Supplier<Collection<EntityChangeSetListener>> entityChangeSetListeners;
    private final Supplier<Collection<EntityChangeListener>> entityChangeListeners;
    private final Supplier<Collection<TransactionListener>> transactionListeners;
    private final EntityManagerFactory emf;
    private Predicate<Object> entityFilter = x -> true;
    private Predicate<Field> fieldFilter = x -> true;
    private LazyIdExtractor lazyIdExtractor;
    private boolean flush = true;

    public DefaultInternalEntityListener(
            EntityManagerFactory emf,
            Supplier<? extends Collection<EntityChangeSetListener>> setListeners,
            Supplier<? extends Collection<EntityChangeListener>> listeners,
            Supplier<? extends Collection<TransactionListener>> transactionListeners)
    {
        this.emf = emf;

        this.entityChangeSetListeners =
                setListeners != null ? (Supplier) setListeners : Collections::emptyList;

        this.entityChangeListeners =
                listeners != null ? (Supplier) listeners : Collections::emptyList;

        this.transactionListeners =
                transactionListeners != null ? (Supplier) transactionListeners : Collections::emptyList;
    }

    // Backward-compatible constructors (still snapshot, but now delegates)
    public DefaultInternalEntityListener(EntityManagerFactory emf, Collection<EntityChangeSetListener> setListeners,
                                         Collection<EntityChangeListener> listeners,
                                         Collection<TransactionListener> transactionListeners)
    {
        this(emf,
                () -> setListeners,
                () -> listeners,
                () -> transactionListeners
        );
    }

    public DefaultInternalEntityListener(EntityManagerFactory emf, EntityChangeSetListener... setListeners)
    {
        this(emf,
                () -> Arrays.asList(setListeners),
                Collections::emptyList,
                Collections::emptyList
        );
    }

    public DefaultInternalEntityListener(EntityManagerFactory emf, EntityChangeListener... listeners)
    {
        this(emf,
                Collections::emptyList,
                () -> Arrays.asList(listeners),
                Collections::emptyList
        );
    }

    public DefaultInternalEntityListener(EntityManagerFactory emf, List<EntityChangeSetListener> listeners)
    {
        this(emf,
                () -> listeners,
                Collections::emptyList,
                Collections::emptyList
        );
    }

    public boolean getFlush()
    {
        return this.flush;
    }

    public DefaultInternalEntityListener setFlush(boolean flush)
    {
        this.flush = flush;
        return this;
    }

    public Predicate<Object> entityFilter()
    {
        return entityFilter;
    }

    public DefaultInternalEntityListener entityFilter(Predicate<Object> entityFilter)
    {
        this.entityFilter = entityFilter;
        return this;
    }

    public Predicate<Field> fieldFilter()
    {
        return fieldFilter;
    }

    public DefaultInternalEntityListener fieldFilter(Predicate<Field> fieldFilter)
    {
        this.fieldFilter = fieldFilter;
        return this;
    }

    @Override
    public void beforeCommit(Object txn)
    {
        if (shouldFlush())
        {
            final EntityManager txnEm = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
            if (txnEm != null)
            {
                txnEm.flush();
            }
        }

        final MutableEntityDataChangeSet cs = preChangeset.get();

        if (!cs.isEmpty())
        {
            lazySetId(cs);

            for (EntityChangeSetListener listener : entityChangeSetListeners.get())
            {
                listener.preDataChanged(MutableEntityDataChangeSet.clone(cs));
            }
        }

        preChangeset.remove();
    }

    protected boolean shouldFlush()
    {
        return flush;
    }

    protected EntityManagerFactory getEmf()
    {
        return emf;
    }

    @Override
    public void afterCommit(Object txn)
    {
        for (TransactionListener listener : transactionListeners.get())
        {
            listener.afterCommit(txn);
        }

        final MutableEntityDataChangeSet cs = postChangeset.get();

        if (!cs.isEmpty())
        {
            lazySetId(cs);

            for (EntityChangeSetListener listener : entityChangeSetListeners.get())
            {
                listener.postDataChanged(cs);
            }
        }

        postChangeset.remove();
    }

    private void lazySetId(MutableEntityDataChangeSet cs)
    {
        if (!cs.isEmpty())
        {
            doSetId(cs.getCreated(), false);
            removeDuplicates(cs.getCreated());

            doSetId(cs.getDeleted(), true);
            removeDuplicates(cs.getDeleted());

            removeDuplicates(cs.getUpdated());
        }
    }

    private void removeDuplicates(Collection<EntityDataChange> coll)
    {
        if (coll.size() > 1)
        {
            final Collection<EntityDataChange> tmp = new LinkedHashSet<>(coll);
            coll.clear();
            coll.addAll(tmp);
        }
    }

    private void doSetId(Collection<EntityDataChange> changeList, boolean deleted)
    {
        if (this.lazyIdExtractor != null)
        {
            for (EntityDataChange change : changeList)
            {
                setIdOnDataChange(change, change.getEntity(), deleted);
            }
        }
    }

    private void setIdOnDataChange(EntityDataChange change, Object entity, boolean deleted)
    {
        final EntityDataChangeImpl impl = (EntityDataChangeImpl) change;

        Serializable id = change.getId() != null
                ? change.getId()
                : lazyIdExtractor.extractId(entity);

        final String[] propertyNames = lazyIdExtractor.extractIdPropertyNames(entity);
        Assert.notNull(propertyNames, "propertyNames cannot be null");

        final Map<Field, Object> idProperties = getIdProperties(propertyNames, id, entity);

        for (Entry<Field, Object> e : idProperties.entrySet())
        {
            final String propName = e.getKey().getName();
            final Optional<PropertyChange<?>> idProp = impl.getPropertyChange(propName);

            if (idProp.isEmpty())
            {
                impl.prependIdPropertyChange(e.getKey(), propName, e.getValue(), deleted);
            }
        }

        impl.setId(id);
    }

    private Map<Field, Object> getIdProperties(String[] propertyNames, Serializable id, Object entity)
    {
        final IdClass idClassAnn = entity.getClass().getAnnotation(IdClass.class);
        final boolean isComposite = idClassAnn != null;

        if (!isComposite)
        {
            return Collections.singletonMap(
                    ReflectionUtils.findField(entity.getClass(), propertyNames[0]),
                    id
            );
        }
        else
        {
            final Map<Field, Object> retVal = new LinkedHashMap<>(propertyNames.length);
            final Class<?> entityIdClass = idClassAnn.value();

            ReflectionUtils.doWithFields(entityIdClass, (f) ->
                    {
                        if (!Modifier.isStatic(f.getModifiers()) && fieldFilter.test(f))
                        {
                            retVal.put(f, ReflectionUtil.get(entity, f.getName()));
                        }
                    }
            );

            return retVal;
        }
    }

    public DefaultInternalEntityListener setLazyIdExtractor(LazyIdExtractor lazyIdExtractor)
    {
        this.lazyIdExtractor = lazyIdExtractor;
        return this;
    }

    private boolean isIgnored(EntityDataChange entityData)
    {
        return entityData.getPropertyChanges().isEmpty()
                || entityData.getEntity().getClass().getAnnotation(EntityListenerIgnore.class) != null
                || !entityFilter.test(entityData.getEntity());
    }

    @Override
    public void preCreate(EntityDataChange entityData)
    {
        if (!isIgnored(entityData))
        {
            for (EntityChangeListener listener : entityChangeListeners.get())
            {
                listener.preCreate(entityData);
            }
        }
    }

    @Override
    public void preUpdate(EntityDataChange entityData)
    {
        if (!isIgnored(entityData))
        {
            for (EntityChangeListener listener : entityChangeListeners.get())
            {
                listener.preUpdate(entityData);
            }
        }
    }

    @Override
    public void preDelete(EntityDataChange entityData)
    {
        if (!isIgnored(entityData))
        {
            for (EntityChangeListener listener : entityChangeListeners.get())
            {
                listener.preDelete(entityData);
            }
        }
    }

    @Override
    public void created(EntityDataChange entityData)
    {
        if (isIgnored(entityData)) return;

        preChangeset.get().getCreated().add(entityData);
        postChangeset.get().getCreated().add(entityData);

        for (EntityChangeListener listener : entityChangeListeners.get())
        {
            listener.created(entityData);
        }
    }

    @Override
    public void updated(EntityDataChange entityData)
    {
        if (isIgnored(entityData)) return;

        preChangeset.get().getUpdated().add(entityData);
        postChangeset.get().getUpdated().add(entityData);

        for (EntityChangeListener listener : entityChangeListeners.get())
        {
            listener.updated(entityData);
        }
    }

    @Override
    public void deleted(EntityDataChange entityData)
    {
        if (isIgnored(entityData)) return;

        setIdOnDataChange(entityData, entityData.getEntity(), true);

        preChangeset.get().getDeleted().add(entityData);
        postChangeset.get().getDeleted().add(entityData);

        for (EntityChangeListener listener : entityChangeListeners.get())
        {
            listener.deleted(entityData);
        }
    }

    @Override
    public void postFlush(Iterator<Object> entities)
    {
        final Set<Object> flushed = new HashSet<>();
        entities.forEachRemaining(flushed::add);

        setAutoIdsAfterFlush(preChangeset.get(), flushed);
        setAutoIdsAfterFlush(postChangeset.get(), flushed);
    }

    private void setAutoIdsAfterFlush(MutableEntityDataChangeSet cs, Collection<Object> flushed)
    {
        for (EntityDataChange ch : cs.getCreated())
        {
            for (Object f : flushed)
            {
                if (ch.getEntity() == f)
                {
                    setIdOnDataChange(ch, f, false);
                }
            }
        }
    }

    @Override
    public void cleanup(Object txn)
    {
        preChangeset.remove();
        postChangeset.remove();

        for (TransactionListener listener : transactionListeners.get())
        {
            listener.afterComplete(txn);
        }
    }

    @Override
    public void rollback(Object txn)
    {
        for (TransactionListener listener : transactionListeners.get())
        {
            listener.afterRollback(txn);
        }

        final MutableEntityDataChangeSet cs = preChangeset.get();

        for (EntityChangeListener listener : entityChangeListeners.get())
        {
            for (EntityDataChange e : cs.getCreated())
                listener.rolledBackCreated(e);

            for (EntityDataChange e : cs.getUpdated())
                listener.rolledBackUpdated(e);

            for (EntityDataChange e : cs.getDeleted())
                listener.rolledBackDeleted(e);
        }
    }
}