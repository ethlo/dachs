package com.ethlo.dachs.test;

import java.util.List;

import javax.persistence.Column;
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
    @Column(name="id")
	private Integer id;
    
    @OneToOne(optional=false)
    @JoinColumn(name="customer_id")
    private Customer customer;
    
    private String notes;
    
    @OneToMany(targetEntity=OrderLine.class)
    @JoinColumn
    private List<OrderLine> lines;
}
