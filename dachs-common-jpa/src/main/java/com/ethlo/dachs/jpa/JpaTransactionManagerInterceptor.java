package com.ethlo.dachs.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.ethlo.dachs.EntityChangeListener;
import com.ethlo.dachs.EntityChangeSetListener;
import com.ethlo.dachs.EntityDataChange;
import com.ethlo.dachs.EntityDataChangeImpl;
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

	private LazyIdExtractor lazyIdExtractor;

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
	protected void doCommit(DefaultTransactionStatus status)
	{
		beforeCommit();

		// Perform commit
		super.doCommit(status);
		
		afterCommit();
	}

	private void beforeCommit()
	{
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
		if (this.lazyIdExtractor != null)
		{
			doSetId(cs.getCreated(), false);
			//doSetId(cs.getUpdated(), false);
			doSetId(cs.getDeleted(), true);
		}
	}

	private void doSetId(List<EntityDataChange> list, boolean deleted)
	{
		for (EntityDataChange created : list)
		{
			final EntityDataChangeImpl impl = (EntityDataChangeImpl) created;
			final Serializable id = lazyIdExtractor.extractId(created.getEntity());
			impl.setId(id);
			final String propertyName = lazyIdExtractor.extractIdPropertyName(created.getEntity());
			impl.prependIdPropertyChange(propertyName, id, deleted);
		}
	}

	public void setLazyIdExtractor(LazyIdExtractor lazyIdExtractor)
	{
		this.lazyIdExtractor = lazyIdExtractor;
	}

	@Override
	public void preCreate(EntityDataChange entityData)
	{
		final MutableEntityDataChangeSet cs = preCs.get();
		cs.getCreated().add(entityData);
		
		for (EntityChangeListener listener : this.entityChangeListeners)
		{
			listener.preCreate(entityData);
		}
	}

	@Override
	public void preUpdate(EntityDataChange entityData)
	{
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
		final MutableEntityDataChangeSet cs = postCs.get();
		cs.getDeleted().add(entityData);
		
		for (EntityChangeListener listener : this.entityChangeListeners)
		{
			listener.deleted(entityData);
		}
	}
}
