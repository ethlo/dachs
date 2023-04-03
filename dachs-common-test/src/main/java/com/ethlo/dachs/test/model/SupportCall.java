package com.ethlo.dachs.test.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@IdClass(SupportCallId.class)
@Table(name = "call_log")
public class SupportCall
{
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Id
    @Column(name = "calltime", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date callTime;

    private String notes;

    public Customer getCustomer()
    {
        return customer;
    }

    public SupportCall setCustomer(Customer customer)
    {
        this.customer = customer;
        return this;
    }

    public Date getCallTime()
    {
        return callTime;
    }

    public SupportCall setCallTime(Date changeTime)
    {
        this.callTime = changeTime;
        return this;
    }

    public String getNotes()
    {
        return notes;
    }

    public SupportCall setNotes(String notes)
    {
        this.notes = notes;
        return this;
    }
}
