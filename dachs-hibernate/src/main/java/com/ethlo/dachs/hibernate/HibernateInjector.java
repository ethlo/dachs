package com.ethlo.dachs.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.persister.entity.EntityPersister;

import com.ethlo.dachs.AuditEntityListener;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.jpa.EntityUtil;

public class HibernateInjector
{
	@SuppressWarnings("serial")
	public static void registerListeners(EntityManagerFactory emf, final AuditEntityListener... auditEntityListener)
	{
		final EntityUtil entityUtil = new EntityUtil(emf.getPersistenceUnitUtil());
		
		final SessionFactoryImpl factory = (SessionFactoryImpl) ((HibernateEntityManagerFactory) emf).getSessionFactory();
		final EventListenerRegistry registry = factory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.POST_COMMIT_INSERT).appendListener(new PostCommitInsertEventListener()
        {
			@Override
			public boolean requiresPostCommitHanding(EntityPersister persister)
			{
				return true;
			}
			
			@Override
			public void onPostInsert(PostInsertEvent event)
			{
				for (AuditEntityListener l : auditEntityListener)
				{
					l.create(event.getId(), event.getEntity(), getProperties(event));
				}
			}

			private Collection<PropertyChange<?>> getProperties(PostInsertEvent event)
			{
				return entityUtil.extractEntityProperties(event.getEntity());
			}

			@Override
			public void onPostInsertCommitFailed(PostInsertEvent event){}
		});
        
        registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(new PostCommitUpdateEventListener()
        {
			@Override
			public boolean requiresPostCommitHanding(EntityPersister persister)
			{
				return true;
			}
			
			@Override
			public void onPostUpdate(PostUpdateEvent event)
			{
				for (AuditEntityListener l : auditEntityListener)
				{
					l.update(event.getId(), event.getEntity(), getProperties(event));
				}
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			private Collection<PropertyChange<?>> getProperties(PostUpdateEvent event)
			{
				final List<PropertyChange<?>> allProperties = entityUtil.extractEntityProperties(event.getEntity());
				final List<PropertyChange<?>> retVal = new ArrayList<>();
				for (int idx : event.getDirtyProperties())
				{
					final PropertyChange<?> chg = allProperties.get(idx);
					final PropertyChange<Object> changed = new PropertyChange(chg.getPropertyName(), chg.getEntityType(), event.getOldState()[idx], chg.getNewValue());
					retVal.add(changed);
				}
				return retVal;
			}

			@Override
			public void onPostUpdateCommitFailed(PostUpdateEvent event) {
				// Ignored
			}
		});     
        
        registry.getEventListenerGroup(EventType.POST_COMMIT_DELETE).appendListener(new PostCommitDeleteEventListener() {
			
			@Override
			public boolean requiresPostCommitHanding(EntityPersister persister)
			{
				return true;
			}
			
			@Override
			public void onPostDelete(PostDeleteEvent event)
			{
				for (AuditEntityListener l : auditEntityListener)
				{
					l.delete(event.getId(), event.getEntity());
				}
			}
			
			@Override
			public void onPostDeleteCommitFailed(PostDeleteEvent event)
			{
				//Ignored
			}
		});
	}
}

