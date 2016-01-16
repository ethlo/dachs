package com.ethlo.dachs.hibernate;

import org.springframework.boot.test.SpringApplicationConfiguration;

import com.ethlo.dachs.test.DataRepositoryTest;

@SpringApplicationConfiguration(classes = HibernateCfg.class)
public class HibernateTest extends DataRepositoryTest{}