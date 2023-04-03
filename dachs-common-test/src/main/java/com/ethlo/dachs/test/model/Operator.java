package com.ethlo.dachs.test.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="operator")
public class Operator
{
    @Id
    @Column(name="user_id")
    private Integer id;
    
    @Column(name="email")
    private String email;

    public Integer getId()
    {
        return id;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
