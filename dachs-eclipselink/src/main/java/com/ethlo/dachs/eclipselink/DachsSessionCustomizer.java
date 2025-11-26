package com.ethlo.dachs.eclipselink;

import java.util.Map;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionCustomizer;

public class DachsSessionCustomizer implements SessionCustomizer
{
    @SuppressWarnings("FieldCanBeLocal")
    private static EclipseLinkToSpringContextBridge handler;

    @Override
    public void customize(Session session)
    {
        handler = new EclipseLinkToSpringContextBridge();

        final Map<Class<?>, ClassDescriptor> descriptors = session.getDescriptors();
        for (ClassDescriptor descriptor : descriptors.values())
        {
            descriptor.getDescriptorEventManager().addEntityListenerEventListener(handler);
        }
    }
}
