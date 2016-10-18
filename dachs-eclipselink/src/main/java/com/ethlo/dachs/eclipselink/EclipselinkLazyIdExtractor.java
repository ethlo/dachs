package com.ethlo.dachs.eclipselink;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import com.ethlo.dachs.LazyIdExtractor;

/**
 * As the IDs might be auto-generated, they are often not available until the database changes are flushed. 
 * This class will attempt to populate the IDs from the entity objects once the session is flushed.
 */
public class EclipselinkLazyIdExtractor implements LazyIdExtractor
{
	private PersistenceUnitUtil persistenceUnitUtil;

	public EclipselinkLazyIdExtractor(EntityManagerFactory emf)
	{
		this.persistenceUnitUtil = emf.getPersistenceUnitUtil();
	}

	@Override
	public Serializable extractId(Object entity)
	{
		return (Serializable) persistenceUnitUtil.getIdentifier(entity);
	}

	@Override
	public String extractIdPropertyName(Object entity)
	{
		return null;
	}
}
