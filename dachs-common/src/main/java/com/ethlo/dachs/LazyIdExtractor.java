package com.ethlo.dachs;

import java.io.Serializable;

public interface LazyIdExtractor
{
	Serializable extractId(Object entity);

	String extractIdPropertyName(Object entity);
}
