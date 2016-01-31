package com.ethlo.dachs.hibernate;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;

import com.ethlo.dachs.LazyIdExtractor;

/**
 * As the IDs might be auto-generated, they are often not available until the database changes are flushed. 
 * This class will attempt to populate the IDs from the entity objects once the session is flushed.
 */
public class HibernateLazyIdExtractor implements LazyIdExtractor
{
	private PersistenceUnitUtil persistenceUnitUtil;
	private SessionFactory factory;

	public HibernateLazyIdExtractor(EntityManagerFactory emf)
	{
		this.persistenceUnitUtil = emf.getPersistenceUnitUtil();
		
		final HibernateEntityManagerFactory emFactory = (HibernateEntityManagerFactory) emf;
		this.factory = emFactory.getSessionFactory();
	}

	@Override
	public Serializable extractId(Object entity)
	{
		return (Serializable) persistenceUnitUtil.getIdentifier(entity);
	}

	@Override
	public String extractIdPropertyName(Object entity)
	{
		final ClassMetadata classMetadata = factory.getClassMetadata(entity.getClass());
		return classMetadata.getIdentifierPropertyName();
	}
}
