package com.ethlo.dachs;

public interface InternalAuditEntityListener extends AuditEntityListener
{
	void start();
	
	void discard();
	
	void flush();

    Iterable<PropertyChange<?>> getPendingChanges();
}