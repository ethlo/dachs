# Dachs - Data Change Snitch

### Rationale

In almost all back-end systems there is the need to notify or update additional data whenever core data changes. It can be sending a notification event, invalidating a cache entry or logging the change to an audit log to mention a few. 

For example Spring has already support for events, and from [Spring 4.2](https://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2#transaction-bound-events) it also has support for send-at-end-of-transaction-events. Further Spring JPA supports [auditing](http://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html#jpa.auditing), however it does not support fetching the actual data, just who changed it and when. There is no recollection of what was changed.

The goal of this project is to have a unified API for multiple datasources to listen for core data changes, that can subsequently be used for whatever purpose you see fit.

### API
The goal is to have a simple, but powerful API to get notifications of all changes, that is `created`, `updated` and `deleted`.

{%highlight java %}
public interface EntityListener
{

	void created(EntityData entityData);

	void updated(EntityData entityData);
	
	void deleted(EntityData entityData);
}

{% endhighlight %}

Where `EntityData` is as simple as:

{%highlight java %}

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

{% endhighlight %}

### Transaction boundaries (if applicable)
Often we do not care for events before they are actually committed. For example, we do not want to store audit data if the transaction was rolled back. Dachs can buffer events until commit if in a transactional context.
