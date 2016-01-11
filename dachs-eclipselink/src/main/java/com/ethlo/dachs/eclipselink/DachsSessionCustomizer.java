package com.ethlo.dachs.eclipselink;

import java.util.Map;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.sessions.Session;

public class DachsSessionCustomizer implements SessionCustomizer
{
	private static EclipseLinkToSpringContextBridge handler;

	@Override
	public void customize(Session session) throws Exception
	{
		handler = new EclipseLinkToSpringContextBridge();
		
		final Map<Class, ClassDescriptor> descriptors = session.getDescriptors();
		for (ClassDescriptor descriptor : descriptors.values())
		{
			final Class<?> clazz = descriptor.getJavaClass();
			descriptor.getDescriptorEventManager().addEntityListenerEventListener(handler);
		}
	}
}
