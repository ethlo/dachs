package com.ethlo.dachs.test.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Field-access entity whose getter returns a different value than the persisted field
 */
@Entity
public class Article
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String note;

    protected Article()
    {
    }

    public Article(String code)
    {
        this.code = code;
    }

    public Long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code == null ? null : code.toUpperCase();
    }

    public Article setNote(String note)
    {
        this.note = note;
        return this;
    }
}
