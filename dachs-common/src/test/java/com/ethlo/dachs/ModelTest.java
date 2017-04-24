package com.ethlo.dachs;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class ModelTest
{
    @Test
    public void testPropertyChangeEquals()
    {
        final PropertyChange<String> a = new PropertyChange<String>("foo", String.class, "bar", "baz", null);
        final PropertyChange<String> b = new PropertyChange<String>("foo", String.class, "bar", "baz", null);
        final PropertyChange<String> c = new PropertyChange<String>("foo", String.class, "bar", "bad", null);
        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(a);
        assertThat(a).isNotEqualTo(c);
        assertThat(b).isNotEqualTo(c);
    }
    
    @Test
    public void testPropertyChangeHashCode()
    {
        final PropertyChange<String> a = new PropertyChange<String>("foo", String.class, "bar", "baz", null);
        final PropertyChange<String> b = new PropertyChange<String>("foo", String.class, "bar", "baz", null);
        final PropertyChange<String> c = new PropertyChange<String>("foo", String.class, "bar", "bad", null);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.hashCode()).isNotEqualTo(c.hashCode());
    }
}
