package com.ethlo.dachs.test.model;

import static jakarta.persistence.CascadeType.ALL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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
    private final Set<String> tags = new HashSet<>();
    
    @OneToMany(mappedBy = "customer", cascade = ALL, orphanRemoval = true)
    @MapKey(name = "name")
    private final Map<String, Category> categories = new HashMap<>();
    
    @Version
    @EntityListenerIgnore
    private Integer version;
    
    @OneToMany(targetEntity=ProductOrder.class, cascade=CascadeType.ALL)
    private final List<ProductOrder> orders = new ArrayList<>();
    
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

    public Set<String> getTags()
    {
        return tags;
    }

    public Integer getVersion()
    {
        return version;
    }

    public List<ProductOrder> getOrders()
    {
        return orders;
    }

    public void addCategory(Category category)
    {
        this.categories.put(category.getName(), category);
    }
}

