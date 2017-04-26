# dachs-eclipselink

This the the [Dachs](https://github.com/ethlo/dachs) module for EclipseLink.

## Setup


### 1. Add dependency
```xml
<dependency>
  <groupId>com.ethlo.dachs</groupId>
  <artifactId>dachs-eclipselink</artifactId>
  <version>${dachs.version}</version>
</dependecy>
```

### 2. Enable automatic configuration using Spring Boot
```properties
spring.jpa.properties.eclipselink.session.customizer=com.ethlo.dachs.eclipselink.DachsSessionCustomizer
```

### 3. Register one or more listeners
```java
@Bean
public EntityChangeSetListener entityChangeSetListener()
{
	return new EntityChangeSetAdapter()
	{
		@Override
		public void postDataChanged(EntityDataChangeSet changes)
		{
			// Will be triggered after commit
		}
	}
}
