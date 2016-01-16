package com.ethlo.dachs;

import java.util.List;

public interface EntityDataChangeSet
{
	List<EntityDataChange> getCreated();

	List<EntityDataChange> getUpdated();

	List<EntityDataChange> getDeleted();
}