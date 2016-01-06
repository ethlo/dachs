# Dachs - Data Change Snitch

### Rationale

In almost all back-end systems there is the need to notify or update additional data whenever core data changes. It can be sending a notification event, invalidating a cache entry or logging the change to an audit log to mention a few. 

For example Spring has already support for events, and from [Spring 4.2](https://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2#transaction-bound-events) it also has support for send-at-end-of-transaction-events. Further Spring JPA supports [auditing](http://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html#jpa.auditing), however it does not support fetching the actual data, just who changed it and when. There is no recollection of what was changed.

The goal of this project is to have a unified API for multiple datasources to listen for core data changes, that can subsequently be used for whatever purpose you see fit.
