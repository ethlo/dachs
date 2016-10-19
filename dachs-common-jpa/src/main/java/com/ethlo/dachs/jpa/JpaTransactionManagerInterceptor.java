package com.ethlo.dachs.jpa;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.ethlo.dachs.EntityChangeListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.EntityDataChangeImpl;
import com.ethlo.dachs.EntityListenerIgnore;
import com.ethlo.dachs.InternalEntityListener;
import com.ethlo.dachs.LazyIdExtractor;
import com.ethlo.dachs.MutableEntityDataChangeSet;

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

	private static final ThreadLocal<MutableEntityDataChangeSet> preCs = new ThreadLocal<MutableEntityDataChangeSet>()
	{
		@Override
		protected MutableEntityDataChangeSet initialValue()
		{
			return new MutableEntityDataChangeSet();
		}
	};
	
	private static final ThreadLocal<MutableEntityDataChangeSet> postCs = new ThreadLocal<MutableEntityDataChangeSet>()
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
		preCs.remove();
		postCs.remove();
		
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
	    
		final MutableEntityDataChangeSet cs = preCs.get();
		lazySetId(cs);
		
		for (EntityChangeSetListener listener : entityChangeSetListeners)
		{
			listener.preDataChanged(cs);
		}
	}

	private void afterCommit()
	{
		final MutableEntityDataChangeSet cs = postCs.get();
		lazySetId(cs);
		for (EntityChangeSetListener listener : entityChangeSetListeners)
		{
			listener.postDataChanged(cs);
		}
	}

	private void lazySetId(MutableEntityDataChangeSet cs)
	{
		doSetId(cs.getCreated(), false);
		//doSetId(cs.getUpdated(), false);
		doSetId(cs.getDeleted(), true);
	}

	private void doSetId(List<EntityDataChange> changeList, boolean deleted)
	{
        if (this.lazyIdExtractor != null)
        {
    		for (EntityDataChange change : changeList)
    		{
    		    final EntityDataChangeImpl impl = (EntityDataChangeImpl) change;
    			final Serializable id = lazyIdExtractor.extractId(change.getEntity());
    			impl.setId(id);
    			final String propertyName = lazyIdExtractor.extractIdPropertyName(change.getEntity());
    			if (propertyName != null)
    			{
    			    impl.prependIdPropertyChange(propertyName, id, deleted);
    			}
    		}
    	}
	}
	
	public JpaTransactionManagerInterceptor setLazyIdExtractor(LazyIdExtractor lazyIdExtractor)
	{
		this.lazyIdExtractor = lazyIdExtractor;
		return this;
	}

	@Override
	public void preCreate(EntityDataChange entityData)
	{
	    if (isIgnored(entityData))
	    {
	        return;
	    }
	    
		final MutableEntityDataChangeSet cs = preCs.get();
		cs.getCreated().add(entityData);
		
		for (EntityChangeListener listener : this.entityChangeListeners)
		{
			listener.preCreate(entityData);
		}
	}

	private boolean isIgnored(EntityDataChange entityData)
    {
	    return entityData.getEntity().getClass().getAnnotation(EntityListenerIgnore.class) != null || !entityFilter.test(entityData.getEntity());
    }

    @Override
	public void preUpdate(EntityDataChange entityData)
	{
        if (isIgnored(entityData))
        {
            return;
        }
        
		final MutableEntityDataChangeSet cs = preCs.get();
		cs.getUpdated().add(entityData);
		
		for (EntityChangeListener listener : this.entityChangeListeners)
		{
			listener.preUpdate(entityData);
		}

	}

	@Override
	public void preDelete(EntityDataChange entityData)
	{
	    if (isIgnored(entityData))
        {
            return;
        }
	    
		final MutableEntityDataChangeSet cs = preCs.get();
		cs.getDeleted().add(entityData);
		
		for (EntityChangeListener listener : this.entityChangeListeners)
		{
			listener.preDelete(entityData);
		}

	}

	@Override
	public void created(EntityDataChange entityData)
	{
	    if (isIgnored(entityData))
        {
            return;
        }
	    
		final MutableEntityDataChangeSet cs = postCs.get();
		cs.getCreated().add(entityData);
		
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
	    
		final MutableEntityDataChangeSet cs = postCs.get();
		cs.getUpdated().add(entityData);
		
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
	    
		final MutableEntityDataChangeSet cs = postCs.get();
		cs.getDeleted().add(entityData);
		
		for (EntityChangeListener listener : this.entityChangeListeners)
		{
			listener.deleted(entityData);
		}
	}
}
