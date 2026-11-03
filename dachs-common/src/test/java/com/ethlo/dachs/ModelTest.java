package com.ethlo.dachs;

import java.util.List;

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

    @Test
    public void testPreviousValueFallsBackToCurrentValue()
    {
        final Person person = new Person("Jane", "Doe", true);
        final EntityDataChange change = new EntityDataChangeImpl(1, person, List.of(new PropertyChange<>("firstName", String.class, "Janet", "Jane")));
        assertThat(change.getPreviousValue("firstName")).isEqualTo("Janet");
        assertThat(change.getPreviousValue("lastName")).isEqualTo("Doe");
        assertThat(change.getPreviousValue("active")).isEqualTo(true);
        assertThat(change.getPreviousValue("nickname")).isEqualTo("JD");
    }

    @Test
    public void testPreviousValueKeepsNullOldValue()
    {
        final Person person = new Person("Jane", "Doe", true);
        final EntityDataChange change = new EntityDataChangeImpl(1, person, List.of(new PropertyChange<>("firstName", String.class, null, "Jane")));
        assertThat(change.getPreviousValue("firstName")).isNull();
    }

    @SuppressWarnings("unused")
    static class Person
    {
        private final String firstName;
        private final String lastName;
        private final boolean active;
        private final String nickname = "JD";

        Person(String firstName, String lastName, boolean active)
        {
            this.firstName = firstName;
            this.lastName = lastName;
            this.active = active;
        }

        public String getFirstName()
        {
            return firstName;
        }

        public String getLastName()
        {
            return lastName;
        }

        public boolean isActive()
        {
            return active;
        }
    }
}
