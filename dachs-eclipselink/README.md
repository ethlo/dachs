###dachs-eclipselink

This module is for Eclipselink.

####Installation

Add dependency to this module in your `pom.xml`:

```xml
<dependency>
  <groupId>com.ethlo.dachs</groupId>
  <artifactId>dachs-eclipselink</artifactId>
  <version>[]</version>
</dependecy>
```

Configure Eclipselink with Dachs:
```java
@Bean
public EntityListener entityListener()
{
  	// Configure _your_ listener here!
	return new MyEntityListener();
}

@Bean
public EclipseLinkToSpringContextBridge eclipseLinkToSpringContextBridge(EntityManagerFactory emf)
{
	final PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
	final EclipseLinkAuditingLoggerHandler handler = new EclipseLinkAuditingLoggerHandler(persistenceUnitUtil, entityListener);
	EclipseLinkToSpringContextBridge.setEntityChangeListener(handler);
	return new EclipseLinkToSpringContextBridge();
}
```
