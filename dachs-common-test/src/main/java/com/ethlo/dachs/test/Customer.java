package com.ethlo.dachs.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.ethlo.dachs.EntityListenerIgnore;

@Table(name="customer")
@Entity
public class Customer
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    private String firstName;
    private String lastName;
    
    @CollectionTable(name="customer_tags")
    @ElementCollection(fetch=FetchType.EAGER)
    private Set<String> tags = new HashSet<>();
    
    @Version
    @EntityListenerIgnore
    private Integer version;
    
    @OneToMany(targetEntity=ProductOrder.class, cascade=CascadeType.ALL)
    private List<ProductOrder> orders = new ArrayList<>();
    
    protected Customer() {}

    public Customer(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public void addTags(String... tags)
    {
    	this.tags.addAll(Arrays.asList(tags));
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

	public void setFirstName(String firstname)
	{
		this.firstName = firstname;
	}

	public String getFirstName()
	{
		return this.firstName;
	}
	
	public String getLastName()
	{
		return this.lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public Long getId()
	{
		return this.id;
	}

	public void addOrder(ProductOrder order)
	{
		this.orders.add(order);
		order.setCustomer(this);
	}
}

