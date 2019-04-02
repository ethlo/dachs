package com.ethlo.dachs.hibernate;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.IdClass;
import javax.persistence.PersistenceUnitUtil;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.util.ReflectionUtils;

import com.ethlo.dachs.LazyIdExtractor;

/**
 * As the IDs might be auto-generated, they are often not available until the database changes are flushed. 
 * This class will attempt to populate the IDs from the entity objects once the session is flushed.
 */
public class HibernateLazyIdExtractor implements LazyIdExtractor
{
	private final PersistenceUnitUtil persistenceUnitUtil;
	private final SessionFactory factory;

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
	public String[] extractIdPropertyNames(Object entity)
	{
	    final IdClass idClassAnn = entity.getClass().getAnnotation(IdClass.class);
	    if (idClassAnn != null)
	    {
	        final Class<?> entityClass = idClassAnn.value();
	        final List<String> retVal = new ArrayList<>(3);
	        ReflectionUtils.doWithFields(entityClass, (f)->
            {
                if (! Modifier.isStatic(f.getModifiers()))
                {
                    retVal.add(f.getName());
                }
            });
	        return retVal.toArray(new String[0]);
	    }
	    else
	    {
	        final ClassMetadata classMetadata = factory.getClassMetadata(entity.getClass());
	        final String propertyName = classMetadata.getIdentifierPropertyName();
	        return propertyName != null ? new String[]{propertyName} : null;
	    }
	}
}
