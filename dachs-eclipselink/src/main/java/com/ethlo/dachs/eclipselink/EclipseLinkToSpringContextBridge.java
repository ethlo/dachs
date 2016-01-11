package com.ethlo.dachs.eclipselink;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;

public class EclipseLinkToSpringContextBridge extends DescriptorEventAdapter
{
	private static EclipseLinkAuditingLoggerHandler handler;

	public static void setEntityChangeListener(EclipseLinkAuditingLoggerHandler handler)
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
}
