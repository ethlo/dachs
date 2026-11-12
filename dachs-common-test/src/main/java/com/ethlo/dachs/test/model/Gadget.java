package com.ethlo.dachs.test.model;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * Entity with a property-access attribute ({@code name}) that has no field of the same name
 */
@Entity
@Access(AccessType.FIELD)
public class Gadget
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private String gadgetName;

    private String note;

    protected Gadget()
    {
    }

    public Gadget(String name)
    {
        this.gadgetName = name;
    }

    public Long getId()
    {
        return id;
    }

    @Access(AccessType.PROPERTY)
    public String getName()
    {
        return gadgetName;
    }

    public void setName(String name)
    {
        this.gadgetName = name;
    }

    public void setNote(String note)
    {
        this.note = note;
    }
}
