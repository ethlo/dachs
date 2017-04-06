package com.ethlo.dachs.util;

public class RuntimeIllegalAccessException extends RuntimeException
{
    private static final long serialVersionUID = -6523064719186094887L;

    public RuntimeIllegalAccessException(IllegalAccessException cause)
    {
        super(cause);
    }
}
