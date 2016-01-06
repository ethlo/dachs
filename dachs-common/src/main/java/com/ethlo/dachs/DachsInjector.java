package com.ethlo.dachs;

public interface DachsInjector
{
	void registerListeners(Object factory, AuditEntityListener... listeners);
}