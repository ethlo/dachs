package com.ethlo.dachs.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import com.ethlo.dachs.EntityListenerIgnore;

@Entity
public class Customer
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String firstName;
    private String lastName;
    
    @ElementCollection(fetch=FetchType.EAGER)
    private Set<String> tags = new HashSet<>();
    
    @Version
    @EntityListenerIgnore
    private Integer version;

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
}

