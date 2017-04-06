package com.ethlo.dachs.test.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.ethlo.dachs.test.model.Customer;

@Transactional
public interface CustomerRepository extends CrudRepository<Customer, Long>
{
    List<Customer> findByLastName(String lastName);
}
