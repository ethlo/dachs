# Dachs - Data Change Snitch

### Goal

To have a unified API for multiple datasources to listen for core data changes that is stable, efficient and non-obtrusive.

### Rationale

In almost all back-end systems there is the need to notify or update additional data whenever core data changes. It can be sending a notification event, invalidating a cache entry or logging the change to an audit log to mention a few. 

For example Spring has already support for events, and from [Spring 4.2](https://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2#transaction-bound-events) it also has support for send-at-end-of-transaction-events. Further Spring JPA supports [auditing](http://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html#jpa.auditing), however it does not support fetching the actual data, just who changed it and when. There is no recollection of what was changed.

### Status
[![Build Status](https://travis-ci.org/ethlo/dachs.png?branch=master)](https://travis-ci.org/ethlo/dachs)

### Supported persistence frameworks
* Eclipselink - [Guide](dachs-eclipselink/README.md)
* Hibernate - [Guide](dachs-hibernate/README.md)

### API
The goal is to have a simple, but powerful API to get notifications of all changes to entities, that is `created`, `updated` and `deleted`.

```java
public interface EntityListener
{
	void created(EntityData entityData);
	void updated(EntityData entityData);
	void deleted(EntityData entityData);
}
```

Using this simple listener, we get an `EntityData` object for each operation on the entity.

```java
public interface EntityData
{

	/**
	 * Returns the id of the entity
	 * @return the id of the entity
	 */
	Serializable getId();

	/**
	 * Returns the entity
	 * @return The entity
	 */
	Object getEntity();

	/**
	 * Get all propertyChanges
	 * @return A list of all property changes for this entity
	 */
	Collection<PropertyChange<?>> getPropertyChanges();

	/**
	 * Get a {@link PropertyChange} for the given propertyName of this entity
	 * @param propertyName The name of the entity property
	 * @return The {@link PropertyChange} for the given propertyName
	 */
	Optional<PropertyChange<?>> getPropertyChange(String propertyName);
```

Each `EntityData` object holds a collection of `PropertyChanges` that is the individual properties that has changed.

```java
public class PropertyChange<T>
{
	public String getPropertyName()
	{
		return propertyName;
	}

	public Class<T> getEntityType()
	{
		return entityType;
	}

	public T getOldValue()
	{
		return oldValue;
	}

	public T getNewValue()
	{
		return newValue;
	}
}
```

####Example output

Given a simple Person object:

```java
public class Person()
{
	private String name;
	private Integer age;
}
```

#####Created
```
EntityData
	propertyChanges:
		* name - null => "John Doe"
		* age - null => 34
```

#####Updated
```
EntityData
	propertyChanges:
		* name - "John Doe" => "John Smith"
		* age - 34 => 47
```

#####Deleted
```
EntityData
	propertyChanges:
		* name - "John Smith" => null
		* age - 47 => null
```

### Transaction boundaries (if applicable)
Often we do not care for events before they are actually committed. For example, we do not want to store audit data if the transaction was rolled back. Dachs can buffer events until commit if in a transactional context.

### Limitations
Dachs relies on the persistence framework in use to notify about operations and there might be limitations. 
In general bulk delete will not trigger delete events (aka `DELETE FROM Entity`). 

### Release history
TBA
