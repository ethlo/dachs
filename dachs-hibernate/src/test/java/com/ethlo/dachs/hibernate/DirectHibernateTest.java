package com.ethlo.dachs.hibernate;

import org.springframework.boot.test.SpringApplicationConfiguration;

import com.ethlo.dachs.test.DirectDataRepositoryTest;

@SpringApplicationConfiguration(classes = HibernateCfg.class)
public class DirectHibernateTest extends DirectDataRepositoryTest{}