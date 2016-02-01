package com.ethlo.dachs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple listener that logs all events to Logback. Mostly useful for testing purposes. 
 */
public class LogbackEntityChangeSetListener extends EntityChangeSetAdapter
{
	private final Logger logger = LoggerFactory.getLogger(LogbackEntityChangeSetListener.class);

	@Override
	public void postDataChanged(EntityDataChangeSet changeset)
	{
		if (! changeset.isEmpty())
		{
			logger.info("{}", changeset);
		}
	}
}