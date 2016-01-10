package com.ethlo.dachs;

import com.ethlo.dachs.EntityListener;
import com.ethlo.dachs.PropertyChange;

public interface BoundaryEntityListenerBuffer extends EntityListener
{
	void start();
	
	void discard();
	
	void flush();

    Iterable<PropertyChange<?>> getPendingChanges();
}