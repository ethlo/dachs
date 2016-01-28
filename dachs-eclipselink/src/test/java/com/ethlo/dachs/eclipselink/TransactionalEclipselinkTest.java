package com.ethlo.dachs.eclipselink;

import org.springframework.boot.test.SpringApplicationConfiguration;

import com.ethlo.dachs.test.TransactionalDataRepositoryTest;

@SpringApplicationConfiguration(classes = EclipselinkCfg.class)
public class TransactionalEclipselinkTest extends TransactionalDataRepositoryTest{}
