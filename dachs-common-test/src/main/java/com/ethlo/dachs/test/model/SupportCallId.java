package com.ethlo.dachs.test.model;

import java.io.Serializable;
import java.util.Date;

public class SupportCallId implements Serializable
{
    private static final long serialVersionUID = 6759669870387591989L;

    private Long customer;
    private Date callTime;
    
    public SupportCallId()
    {

    }
    
    public SupportCallId(long customer, Date callTime)
    {
        this.customer = customer;
        this.callTime = callTime;
    }
    
    public void setCustomer(Long customer)
    {
        this.customer = customer;
    }

    public void setCallTime(Date callTime)
    {
        this.callTime = callTime;
    }

    public Long getCustomer()
    {
        return customer;
    }

    public Date getCallTime()
    {
        return callTime;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callTime == null) ? 0 : callTime.hashCode());
        result = prime * result + ((customer == null) ? 0 : customer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SupportCallId other = (SupportCallId) obj;
        if (callTime == null)
        {
            if (other.callTime != null)
                return false;
        }
        else if (!callTime.equals(other.callTime))
            return false;
        if (customer == null)
        {
            if (other.customer != null)
                return false;
        }
        else if (!customer.equals(other.customer))
            return false;
        return true;
    }
}
