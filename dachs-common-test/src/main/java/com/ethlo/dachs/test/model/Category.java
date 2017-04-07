package com.ethlo.dachs.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
