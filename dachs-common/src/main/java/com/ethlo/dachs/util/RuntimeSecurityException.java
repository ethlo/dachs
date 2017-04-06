package com.ethlo.dachs.util;

public class RuntimeSecurityException extends RuntimeException
{
    private static final long serialVersionUID = -6523064719186094887L;

    public RuntimeSecurityException(SecurityException cause)
    {
        super(cause);
    }
}
