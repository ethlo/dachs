package com.ethlo.dachs.test.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.ethlo.dachs.test.model.SupportCall;
import com.ethlo.dachs.test.model.SupportCallId;

@Transactional
public interface CallRepository extends CrudRepository<SupportCall, SupportCallId>
{
    
}
