package com.ethlo.dachs.eclipselink;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;

public class EclipseLinkToSpringContextBridge extends DescriptorEventAdapter
{
	private static EclipseLinkEntityEventListener handler;

	public static void setEntityChangeListener(EclipseLinkEntityEventListener handler)
	{
		EclipseLinkToSpringContextBridge.handler = handler;
	}

	@Override
	public void postDelete(DescriptorEvent event)
	{
		handler.postRemoveEvent(event);
	}

	@Override
	public void postInsert(DescriptorEvent event)
	{
		handler.postPersistEvent(event);
	}

	@Override
	public void postUpdate(DescriptorEvent event)
	{
		handler.postUpdateEvent(event);
	}

	@Override
	public void prePersist(DescriptorEvent event)
	{
		handler.prePersistEvent(event);
	}

	@Override
	public void preDelete(DescriptorEvent event)
	{
		handler.preRemoveEvent(event);
	}

	@Override
	public void preUpdate(DescriptorEvent event)
	{
		handler.preUpdateEvent(event);
	}
}
