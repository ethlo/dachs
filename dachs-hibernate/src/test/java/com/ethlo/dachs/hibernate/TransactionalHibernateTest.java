package com.ethlo.dachs.hibernate;

import org.springframework.boot.test.context.SpringBootTest;

import com.ethlo.dachs.test.TransactionalDataRepositoryTest;

@SpringBootTest(classes = HibernateCfg.class)
public class TransactionalHibernateTest extends TransactionalDataRepositoryTest{}