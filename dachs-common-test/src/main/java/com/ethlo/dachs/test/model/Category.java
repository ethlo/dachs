package com.ethlo.dachs.test.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@IdClass(CategoryId.class)
@Table(name = "category")
public class Category
{
    @Id
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Id
    @Column(name = "name")
    private String name;

    public Customer getCustomer()
    {
        return customer;
    }

    public Category setCustomer(Customer customer)
    {
        this.customer = customer;
        customer.addCategory(this);
        return this;
    }

    public String getName()
    {
        return name;
    }

    public Category setName(String name)
    {
        this.name = name;
        return this;
    }
}
