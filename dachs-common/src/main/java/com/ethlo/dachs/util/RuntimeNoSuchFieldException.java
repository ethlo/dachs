package com.ethlo.dachs.util;

public class RuntimeNoSuchFieldException extends RuntimeException
{
    private static final long serialVersionUID = -399272769854427264L;

    public RuntimeNoSuchFieldException(NoSuchFieldException cause)
    {
        super(cause);
    }
}
