# dachs-hibernate

This the the [Dachs](https://github.com/ethlo/dachs) module for Hibernate.

## Setup

### 1. Add dependency
```xml
<dependency>
  <groupId>com.ethlo.dachs</groupId>
  <artifactId>dachs-hibernate</artifactId>
  <version>${dachs.version}</version>
</dependecy>
```

### 2. Enable automatic configuration using Spring Boot
```properties
spring.jpa.properties.hibernate.ejb.interceptor=com.ethlo.dachs.hibernate.HibernatePropertyChangeInterceptorBridge
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
```

### Tips

#### Auditing and logging of changes
For auditing purposes, one normally is not interested in an entity's related entities. For example when updating an order that has a reference to a product, we do not want to traverse and log the prodduct entity, we just want to know that the order now references it. 

To serialize these changes (and avoiding a huge, potentially recursive graph), Dachs supports `IdentityUtil.toIndentityReferences(Collection<EntityDataChange> list)`
