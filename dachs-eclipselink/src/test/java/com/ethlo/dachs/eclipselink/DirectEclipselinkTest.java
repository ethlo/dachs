package com.ethlo.dachs.eclipselink;

import org.springframework.boot.test.context.SpringBootTest;

import com.ethlo.dachs.test.repository.DirectDataRepositoryTest;

@SpringBootTest(classes = EclipselinkCfg.class)
public class DirectEclipselinkTest extends DirectDataRepositoryTest{}
