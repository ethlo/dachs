package com.ethlo.dachs.test.model;

import java.util.Currency;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product
{
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
    
	private String name;
	private Currency currency;
	private Integer priceInCents;
	
	protected Product()
	{
		
	}
	
    public Product(String name, int priceInCents, Currency currency)
    {
		this.name = name;
		this.priceInCents = priceInCents;
		this.currency = currency;
	}

    public Integer getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public Integer getPriceInCents()
    {
        return priceInCents;
    }
}
