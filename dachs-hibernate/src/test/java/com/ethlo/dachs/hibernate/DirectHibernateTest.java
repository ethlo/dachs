package com.ethlo.dachs.hibernate;

import org.springframework.boot.test.context.SpringBootTest;

import com.ethlo.dachs.test.repository.DirectDataRepositoryTest;

@SpringBootTest(classes = HibernateCfg.class)
public class DirectHibernateTest extends DirectDataRepositoryTest{}