package com.ethlo.dachs.test.model;

import java.io.Serializable;
import java.util.Objects;

public class CategoryId implements Serializable
{
    private static final long serialVersionUID = 1985372010095687153L;
    
    private Long customer;
    private String name;

    public Long getCustomer()
    {
        return customer;
    }

    public void setCustomer(Long customer)
    {
        this.customer = customer;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((customer == null) ? 0 : customer.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof CategoryId)
        {
            final CategoryId b = (CategoryId) obj;
            return Objects.equals(customer, b.customer) && Objects.equals(name, b.name);
        }
        return false;
    }
}
