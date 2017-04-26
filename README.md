# Dachs - Data Change Snitch
[![Build Status](https://travis-ci.org/ethlo/dachs.svg?branch=master)](https://travis-ci.org/ethlo/dachs)
[![Maven Central](https://img.shields.io/maven-central/v/com.ethlo.dachs/dachs.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.ethlo.dachs%22)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](LICENSE)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9b2a46c2ffdb4c86ad971eec64a06e8b)](https://www.codacy.com/app/ethlo/dachs?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ethlo/dachs&amp;utm_campaign=Badge_Grade)

A _unified_ entity change-listener across different persistence APIs and implementations.

### Rationale

In almost all back-end systems there is the need to notify or update additional data whenever core data changes. It can be sending a notification event, invalidating cache entries, audit logging or updating a search index to mention a few. 

For example Spring has already support for events, and from [Spring 4.2](https://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2#transaction-bound-events) it also has support for send-at-end-of-transaction-events. Further Spring JPA supports [auditing](http://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html#jpa.auditing), however it does not support fetching the actual data, just _who_ changed it and _when_. There is no recollection of _what_ was changed.

![Dachs flow](/resources/dachs_flow.png)

All the different persistence frameworks have their different APIs for detecting data changes. With Dachs you have one simple, unified API to deal with.

### Maven artifact

```xml
<dependency>
  <groupId>com.ethlo.dachs</groupId>
  <artifactId>dachs-{impl}</artifactId>
  <version>${dachs.version}</version>
</dependency>
```

### Supported persistence frameworks
* [Eclipselink - Setup guide](dachs-eclipselink)
* [Hibernate - Setup guide](dachs-hibernate)

### API
The goal is to have a simple, but powerful API to get notifications of all changes to entities, that is `created`, `updated` and `deleted`.

##### Transactional listener

This listener will buffer all events until the transaction is about to commit. `preDataChanged` is called just before the transaction is committed. Any exception here will abort the transaction. `postDataChanged` is called just after the transaction is committed. 

```java
public interface EntityChangeSetListener
{
  void preDataChanged(EntityDataChangeSet changeset);
  void postDataChanged(EntityDataChangeSet changeset);
}
```

Both methods gives you an [`EntityDataChangeSet`](https://github.com/ethlo/dachs/blob/master/dachs-common/src/main/java/com/ethlo/dachs/EntityDataChangeSet.java) whichs holds all operations performed inside the transaction.
```java
public interface EntityDataChangeSet
{
  List<EntityDataChange> getCreated();
  List<EntityDataChange> getUpdated();
  List<EntityDataChange> getDeleted();
  boolean isEmpty();
}
```

##### Non-transactional listener

 [EntityChangeListener](https://github.com/ethlo/dachs/blob/master/dachs-common/src/main/java/com/ethlo/dachs/EntityChangeListener.java)
```java
public interface EntityChangeListener
{
  void preCreate(EntityDataChange entityData);
  void preUpdate(EntityDataChange entityData);
  void preDelete(EntityDataChange entityData);
  void created(EntityDataChange entityData);
  void updated(EntityDataChange entityData);
  void deleted(EntityDataChange entityData);
}
```

Using this simple listener, we get an [`EntityDataChange`](https://github.com/ethlo/dachs/blob/master/dachs-common/src/main/java/com/ethlo/dachs/EntityDataChange.java) object for each operation on the entity.

##### Common for both trasactional and non-transactional
```java
public interface EntityDataChange
{
  Serializable getId();
  Object getEntity();
  List<PropertyChange<?>> getPropertyChanges();
  Optional<PropertyChange<?>> getPropertyChange(String propertyName);
}
```

Each `EntityDataChange` object holds a collection of [`PropertyChange`s](https://github.com/ethlo/dachs/blob/master/dachs-common/src/main/java/com/ethlo/dachs/PropertyChange.java) that is the individual properties that has changed.

```java
public interface PropertyChange<T>
{
  String getPropertyName();
  Class<T> getPropertyType();
  T getOldValue();
  T getNewValue();
}
```
#### Example output

Given a simple Person object:

```java
public class Person()
{
  private String name;
  private Integer age;
}
```

##### Created
```
EntityData
  propertyChanges:
    * name - null => "John Doe"
    * age - null => 34
```

##### Updated
```
EntityData
  propertyChanges:
    * name - "John Doe" => "John Smith"
    * age - 34 => 47
```

##### Deleted
```
EntityData
  propertyChanges:
    * name - "John Smith" => null
    * age - 47 => null
```

### Limitations
Dachs relies on the persistence framework in use to notify about operations and there might be limitations. 
In general bulk delete will not trigger delete events (aka `DELETE FROM Entity`). 
