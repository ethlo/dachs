###Dachs for hibernate

This module is for Hibernate.

####Installation

Add dependency to this module in your `pom.xml`:

```xml
<dependency>
  <groupId>com.ethlo.dachs</groupId>
  <artifactId>dachs-hibernate</artifactId>
  <version>[]</version>
</dependecy>
```

Configure Hibernate with Dachs:
```java
final EntityListener myListener = ...;
final HibernatePropertyChangeInterceptor interceptor = new HibernatePropertyChangeInterceptor(myListener);
cfgMap.put("hibernate.ejb.interceptor", interceptor);
```
