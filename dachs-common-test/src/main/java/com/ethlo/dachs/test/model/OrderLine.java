package com.ethlo.dachs.test.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class OrderLine
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="product_id")
	private Product product;
	
	@ManyToOne
	@JoinColumn(name="order_id")
	private ProductOrder order;

	private Integer count;
	
	private Integer amount;
	
	public OrderLine setProduct(Product product)
	{
		this.product = product;
		return this;
	}
	
	public Integer getId()
	{
		return this.id;
	}

    public OrderLine setOrder(ProductOrder order)
    {
        this.order = order;
        return this;
    }

    public OrderLine setCount(Integer count)
    {
        this.count = count;
        return this;
    }

    public OrderLine setAmount(Integer amount)
    {
        this.amount = amount;
        return this;
    }

    public Product getProduct()
    {
        return product;
    }

    public ProductOrder getOrder()
    {
        return order;
    }

    public Integer getCount()
    {
        return count;
    }

    public Integer getAmount()
    {
        return amount;
    }
}
