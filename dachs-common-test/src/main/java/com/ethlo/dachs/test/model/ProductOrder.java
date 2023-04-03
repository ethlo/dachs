package com.ethlo.dachs.test.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
