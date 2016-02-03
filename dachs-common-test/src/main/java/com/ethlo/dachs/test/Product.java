package com.ethlo.dachs.test;

import java.util.Currency;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Product
{
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
    
	private String name;
	private Currency currency;
	private int priceInCents;
	
	protected Product()
	{
		
	}
	
    public Product(String name, int priceInCents, Currency currency)
    {
		this.name = name;
		this.priceInCents = priceInCents;
		this.currency = currency;
	}
}
