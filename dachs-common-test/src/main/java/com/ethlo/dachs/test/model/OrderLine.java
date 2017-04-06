package com.ethlo.dachs.test.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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
