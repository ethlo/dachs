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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.IdClass;

import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
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
import com.ethlo.dachs.util.ReflectionUtil;

/**
 * Caches entity changes until the transaction commits, or discards them in case of a roll-back.
 */
public class JpaTransactionManagerInterceptor extends JpaTransactionManager implements InternalEntityListener
{
	private static final long serialVersionUID = -6562131067110759051L;
	
	private final Set<EntityChangeSetListener> entityChangeSetListeners;
	private final Set<EntityChangeListener> entityChangeListeners;
    private Predicate<Object> entityFilter = x -> true;
    private Predicate<Field> fieldFilter = x -> true;
	private LazyIdExtractor lazyIdExtractor;
	private EntityManagerFactory emf;
	private boolean flush = true;

	private static final ThreadLocal<MutableEntityDataChangeSet> preChangeset = new ThreadLocal<MutableEntityDataChangeSet>()
	{
		@Override
		protected MutableEntityDataChangeSet initialValue()
		{
			return new MutableEntityDataChangeSet();
		}
	};
	
	private static final ThreadLocal<MutableEntityDataChangeSet> postChangeset = new ThreadLocal<MutableEntityDataChangeSet>()
    {
        @Override
        protected MutableEntityDataChangeSet initialValue()
        {
            return new MutableEntityDataChangeSet();
        }
    };

	public JpaTransactionManagerInterceptor(EntityManagerFactory emf, Collection<EntityChangeSetListener> setListeners, Collection<EntityChangeListener> listeners)
	{
	    this.emf = emf;
		this.entityChangeSetListeners = new LinkedHashSet<>(setListeners);
		this.entityChangeListeners = new LinkedHashSet<>(listeners);
	}

	public JpaTransactionManagerInterceptor(EntityManagerFactory emf, EntityChangeSetListener... setListeners)
	{
		this(emf, Arrays.asList(setListeners), Collections.emptyList());
	}
	
	public JpaTransactionManagerInterceptor(EntityManagerFactory emf, EntityChangeListener... listeners)
	{
		this(emf, Collections.emptyList(), Arrays.asList(listeners));
	}
	
	public JpaTransactionManagerInterceptor setFlush(boolean flush)
	{
	    this.flush = flush;
	    return this;
	}
	
	public boolean getFlush()
    {
        return this.flush;
    }
	
	public Predicate<Object> getEntityFilter()
    {
        return entityFilter;
    }

    public JpaTransactionManagerInterceptor setEntityFilter(Predicate<Object> entityFilter)
    {
        this.entityFilter = entityFilter;
        return this;
    }

    public Predicate<Field> getFieldFilter()
    {
        return fieldFilter;
    }

    public JpaTransactionManagerInterceptor setFieldFilter(Predicate<Field> fieldFilter)
    {
        this.fieldFilter = fieldFilter;
        return this;
    }

    @Override
	protected void doBegin(Object transaction, TransactionDefinition definition)
	{
		super.doBegin(transaction, definition);
		preChangeset.remove();
		postChangeset.remove();
		
		for (EntityChangeSetListener listener : entityChangeSetListeners)
		{
			listener.begin();
		}
	}

	@Override
	protected void prepareForCommit(DefaultTransactionStatus status)
	{
	    beforeCommit();
    }
	
	@Override
	protected void doCommit(DefaultTransactionStatus status)
	{
		super.doCommit(status);
		afterCommit();
	}

	private void beforeCommit()
	{
	    if (flush)
	    {
	        EntityManagerFactoryUtils.getTransactionalEntityManager(emf).flush();
	    }
	    
		final MutableEntityDataChangeSet cs = preChangeset.get();
		
		if (! cs.isEmpty())
		{
    		lazySetId(cs);
    		
    		for (EntityChangeSetListener listener : entityChangeSetListeners)
    		{
    		    listener.preDataChanged(cs);
    		}
		}
		
		preChangeset.remove();
	}

	private void afterCommit()
	{
		final MutableEntityDataChangeSet cs = postChangeset.get();
		if (! cs.isEmpty())
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
	    if (! cs.isEmpty())
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
	    
	    Serializable id = null;
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
	    
	    final Map<String, Object> idProperties = getIdProperties(propertyNames, id, entity);
	    
	    for (String propertyName : propertyNames)
	    {
    	    final Optional<PropertyChange<?>> idProp = impl.getPropertyChange(propertyName);
    	    if (! idProp.isPresent())
            {
    	        final Object value = idProperties.get(propertyName);
                impl.prependIdPropertyChange(propertyName, value, deleted);
            }
	    }
	    
	    impl.setId(id);
    }

    private Map<String, Object> getIdProperties(String[] propertyNames, Serializable id, Object entity)
    {
        final IdClass idClassAnn = entity.getClass().getAnnotation(IdClass.class);
        final boolean isComposite = idClassAnn != null;
        
        if (! isComposite)
        {
            return Collections.singletonMap(propertyNames[0], id);
        }
        else
        {
            // We need to extract the id properties from the composite object
            final Map<String, Object> retVal = new LinkedHashMap<>(propertyNames.length);
            final Class<?> entityIdClass = idClassAnn.value();
            ReflectionUtils.doWithFields(entityIdClass, (f)->
            {
                if (! Modifier.isStatic(f.getModifiers()))
                {
                    final String fieldName = f.getName();
                    retVal.put(fieldName, ReflectionUtil.get(entity, fieldName));
                }
            });
            return retVal;
        }
    }
    
    public JpaTransactionManagerInterceptor setLazyIdExtractor(LazyIdExtractor lazyIdExtractor)
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
        if (! isIgnored(entityData))
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
        if (! isIgnored(entityData))
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
	    if (! isIgnored(entityData))
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
}
