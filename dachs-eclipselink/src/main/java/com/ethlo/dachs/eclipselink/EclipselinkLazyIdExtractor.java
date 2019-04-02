package com.ethlo.dachs.eclipselink;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import com.ethlo.dachs.LazyIdExtractor;

/**
 * As the IDs might be auto-generated, they are often not available until the database changes are flushed. 
 * This class will attempt to populate the IDs from the entity objects once the session is flushed.
 */
public class EclipselinkLazyIdExtractor implements LazyIdExtractor
{
	private final PersistenceUnitUtil persistenceUnitUtil;
    private final EntityManagerFactory emf;

	public EclipselinkLazyIdExtractor(EntityManagerFactory emf)
	{
	    this.emf = emf;
		this.persistenceUnitUtil = emf.getPersistenceUnitUtil();
	}

	@Override
	public Serializable extractId(Object entity)
	{
		return (Serializable) persistenceUnitUtil.getIdentifier(entity);
	}

	@Override
	public String[] extractIdPropertyNames(Object entity)
	{
	    final EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
	    final ClassDescriptor desc = Objects.requireNonNull(em).unwrap(JpaEntityManager.class).getServerSession().getClassDescriptor(entity);
	    if (desc != null)
	    {
    	    final Collection<DatabaseMapping> fieldNames = desc.getMappings();
    	    final List<DatabaseMapping> tmp = new LinkedList<>();
    	    for (DatabaseMapping m : fieldNames)
    	    {
    	        if (m.isPrimaryKeyMapping())
    	        {
    	            tmp.add(m);
    	        }
    	    }
    	    final String[] retVal = new String[tmp.size()];
    	    for (int i = 0; i < retVal.length; i++)
    	    {
    	        retVal[i] = tmp.get(i).getAttributeName();
    	    }
    	    return retVal;
	    }
	    
	    return null;
	}
}
