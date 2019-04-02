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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.IdClass;

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

/**
 * Caches entity changes until the transaction commits, or discards them in case of a roll-back.
 */
public class DefaultInternalEntityListener implements InternalEntityListener, Serializable
{
    private static final long serialVersionUID = -6562131067110759051L;

    private final Set<EntityChangeSetListener> entityChangeSetListeners;
    private final Set<EntityChangeListener> entityChangeListeners;
    private final Set<TransactionListener> transactionListeners;
    private Predicate<Object> entityFilter = x -> true;
    private Predicate<Field> fieldFilter = x -> true;
    private LazyIdExtractor lazyIdExtractor;
    private final EntityManagerFactory emf;
    private boolean flush = true;

    private static final ThreadLocal<MutableEntityDataChangeSet> preChangeset = ThreadLocal.withInitial(MutableEntityDataChangeSet::new);

    private static final ThreadLocal<MutableEntityDataChangeSet> postChangeset = ThreadLocal.withInitial(MutableEntityDataChangeSet::new);

    public DefaultInternalEntityListener(EntityManagerFactory emf, Collection<EntityChangeSetListener> setListeners, Collection<EntityChangeListener> listeners, Collection<TransactionListener> transactionListeners)
    {
        this.emf = emf;
        this.entityChangeSetListeners = setListeners != null ? new LinkedHashSet<>(setListeners) : Collections.emptySet();
        this.entityChangeListeners = listeners != null ? new LinkedHashSet<>(listeners) : Collections.emptySet();
        this.transactionListeners = transactionListeners != null ? new LinkedHashSet<>(transactionListeners) : Collections.emptySet();
    }

    public DefaultInternalEntityListener(EntityManagerFactory emf, EntityChangeSetListener... setListeners)
    {
        this(emf, Arrays.asList(setListeners), Collections.emptyList(), Collections.emptyList());
    }

    public DefaultInternalEntityListener(EntityManagerFactory emf, EntityChangeListener... listeners)
    {
        this(emf, Collections.emptyList(), Arrays.asList(listeners), Collections.emptyList());
    }

    public DefaultInternalEntityListener(EntityManagerFactory emf, List<EntityChangeSetListener> listeners)
    {
        this(emf, listeners, Collections.emptyList(), Collections.emptyList());
    }

    public DefaultInternalEntityListener setFlush(boolean flush)
    {
        this.flush = flush;
        return this;
    }

    public boolean getFlush()
    {
        return this.flush;
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
        if (flush)
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

            for (EntityChangeSetListener listener : entityChangeSetListeners)
            {
                listener.preDataChanged(MutableEntityDataChangeSet.clone(cs));
            }
        }

        preChangeset.remove();
    }

    @Override
    public void afterCommit(Object txn)
    {
        for (TransactionListener listener : this.transactionListeners)
        {
            listener.afterCommit(txn);
        }

        final MutableEntityDataChangeSet cs = postChangeset.get();
        if (!cs.isEmpty())
        {
            lazySetId(cs);
            for (EntityChangeSetListener listener : entityChangeSetListeners)
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
            // We need this to be done separately as we mutate the ID property
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

        Serializable id;
        if (change.getId() == null)
        {
            id = lazyIdExtractor.extractId(entity);
        }
        else
        {
            id = change.getId();
        }

        final String[] propertyNames = lazyIdExtractor.extractIdPropertyNames(entity);
        Assert.notNull(propertyNames, "propertyNames cannot be null");

        final Map<Field, Object> idProperties = getIdProperties(propertyNames, id, entity);

        for (Entry<Field, Object> e : idProperties.entrySet())
        {
            final String propName = e.getKey().getName();
            final Optional<PropertyChange<?>> idProp = impl.getPropertyChange(propName);
            if (!idProp.isPresent())
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
            return Collections.singletonMap(ReflectionUtils.findField(entity.getClass(), propertyNames[0]), id);
        }
        else
        {
            // We need to extract the id properties from the composite object
            final Map<Field, Object> retVal = new LinkedHashMap<>(propertyNames.length);
            final Class<?> entityIdClass = idClassAnn.value();
            ReflectionUtils.doWithFields(entityIdClass, (f) ->
            {
                if (!Modifier.isStatic(f.getModifiers()) && fieldFilter.test(f))
                {
                    final String fieldName = f.getName();
                    retVal.put(f, ReflectionUtil.get(entity, fieldName));
                }
            });
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
            for (EntityChangeListener listener : this.entityChangeListeners)
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
            for (EntityChangeListener listener : this.entityChangeListeners)
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
            for (EntityChangeListener listener : this.entityChangeListeners)
            {
                listener.preDelete(entityData);
            }
        }
    }

    @Override
    public void created(EntityDataChange entityData)
    {
        if (isIgnored(entityData))
        {
            return;
        }

        final MutableEntityDataChangeSet preCs = preChangeset.get();
        preCs.getCreated().add(entityData);
        final MutableEntityDataChangeSet postCs = postChangeset.get();
        postCs.getCreated().add(entityData);

        for (EntityChangeListener listener : this.entityChangeListeners)
        {
            listener.created(entityData);
        }
    }

    @Override
    public void updated(EntityDataChange entityData)
    {
        if (isIgnored(entityData))
        {
            return;
        }

        final MutableEntityDataChangeSet preCs = preChangeset.get();
        preCs.getUpdated().add(entityData);
        final MutableEntityDataChangeSet postCs = postChangeset.get();
        postCs.getUpdated().add(entityData);

        for (EntityChangeListener listener : this.entityChangeListeners)
        {
            listener.updated(entityData);
        }
    }

    @Override
    public void deleted(EntityDataChange entityData)
    {
        if (isIgnored(entityData))
        {
            return;
        }

        setIdOnDataChange(entityData, entityData.getEntity(), true);

        final MutableEntityDataChangeSet preCs = preChangeset.get();
        preCs.getDeleted().add(entityData);
        final MutableEntityDataChangeSet postCs = postChangeset.get();
        postCs.getDeleted().add(entityData);

        for (EntityChangeListener listener : this.entityChangeListeners)
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
            final Object chEntity = ch.getEntity();
            for (Object f : flushed)
            {
                if (chEntity == f)
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

        for (TransactionListener listener : this.transactionListeners)
        {
            listener.afterComplete(txn);
        }
    }

    @Override
    public void rollback(Object txn)
    {
        for (TransactionListener listener : this.transactionListeners)
        {
            listener.afterRollback(txn);
        }

        final MutableEntityDataChangeSet preCs = preChangeset.get();
        for (EntityChangeListener listener : this.entityChangeListeners)
        {
            for (EntityDataChange e : preCs.getCreated())
            {
                listener.rolledBackCreated(e);
            }

            for (EntityDataChange e : preCs.getUpdated())
            {
                listener.rolledBackUpdated(e);
            }

            for (EntityDataChange e : preCs.getDeleted())
            {
                listener.rolledBackDeleted(e);
            }
        }
    }
}
