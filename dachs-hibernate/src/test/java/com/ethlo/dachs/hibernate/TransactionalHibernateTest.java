package com.ethlo.dachs.hibernate;

import org.springframework.boot.test.SpringApplicationConfiguration;

import com.ethlo.dachs.test.TransactionalDataRepositoryTest;

@SpringApplicationConfiguration(classes = HibernateCfg.class)
public class TransactionalHibernateTest extends TransactionalDataRepositoryTest{}