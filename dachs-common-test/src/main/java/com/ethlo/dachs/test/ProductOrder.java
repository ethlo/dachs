package com.ethlo.dachs.test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="product_order")
public class ProductOrder
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
    
    @OneToOne
    @JoinColumn(name="customer_id")
    private Customer customer;
    
    private String notes;
    
    @OneToMany(targetEntity=OrderLine.class, cascade=CascadeType.ALL)
    @JoinColumn
    private List<OrderLine> lines = new ArrayList<>();

	public void addLine(OrderLine orderLine)
	{
		this.lines.add(orderLine);
	}

	public void setCustomer(Customer customer)
	{
		this.customer = customer;
	}
}
