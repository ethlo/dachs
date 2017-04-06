package com.ethlo.dachs.test.model;

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

	public ProductOrder setCustomer(Customer customer)
	{
		this.customer = customer;
		return this;
	}

    public String getNotes()
    {
        return notes;
    }

    public ProductOrder setNotes(String notes)
    {
        this.notes = notes;
        return this;
    }

    public List<OrderLine> getLines()
    {
        return lines;
    }

    public ProductOrder setLines(List<OrderLine> lines)
    {
        this.lines = lines;
        return this;
    }

    public Integer getId()
    {
        return id;
    }

    public Customer getCustomer()
    {
        return customer;
    }
}
