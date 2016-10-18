package com.ethlo.dachs.eclipselink;

import org.springframework.boot.test.context.SpringBootTest;

import com.ethlo.dachs.test.TransactionalDataRepositoryTest;

@SpringBootTest(classes = EclipselinkCfg.class)
public class TransactionalEclipselinkTest extends TransactionalDataRepositoryTest{}
