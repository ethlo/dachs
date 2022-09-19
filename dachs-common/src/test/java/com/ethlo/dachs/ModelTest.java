package com.ethlo.dachs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelTest
{
    @Test
    public void testPropertyChangeEquals()
    {
        final PropertyChange<String> a = new PropertyChange<>("foo", String.class, "bar", "baz");
        final PropertyChange<String> b = new PropertyChange<>("foo", String.class, "bar", "baz");
        final PropertyChange<String> c = new PropertyChange<>("foo", String.class, "bar", "bad");
        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(a);
        assertThat(a).isNotEqualTo(c);
        assertThat(b).isNotEqualTo(c);
    }
    
    @Test
    public void testPropertyChangeHashCode()
    {
        final PropertyChange<String> a = new PropertyChange<>("foo", String.class, "bar", "baz");
        final PropertyChange<String> b = new PropertyChange<>("foo", String.class, "bar", "baz");
        final PropertyChange<String> c = new PropertyChange<>("foo", String.class, "bar", "bad");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.hashCode()).isNotEqualTo(c.hashCode());
    }
}
