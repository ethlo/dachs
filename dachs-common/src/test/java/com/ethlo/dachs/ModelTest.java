package com.ethlo.dachs;

import java.lang.reflect.Proxy;
import java.util.List;

import jakarta.persistence.metamodel.Attribute;

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
    public void testPreviousValueFallsBackToCurrentFieldValue()
    {
        final Person person = new Person("Jane", "Doe");
        final EntityDataChange change = new EntityDataChangeImpl(1, person, List.of(new PropertyChange<>("firstName", String.class, "Janet", "Jane")));
        assertThat(change.getPreviousValue("firstName")).isEqualTo("Janet");
        assertThat(change.getPreviousValue("lastName")).isEqualTo("Doe");
    }

    @Test
    public void testPreviousValueKeepsNullOldValue()
    {
        final Person person = new Person("Jane", "Doe");
        final EntityDataChange change = new EntityDataChangeImpl(1, person, List.of(new PropertyChange<>("firstName", String.class, null, "Jane")));
        assertThat(change.getPreviousValue("firstName")).isNull();
    }

    @Test
    public void testPreviousValueUsesCurrentValueReaderAlsoInCopies()
    {
        final Person person = new Person("Jane", "Doe");
        final EntityDataChange change = new EntityDataChangeImpl(1, person, List.of(), name -> "read:" + name);
        assertThat(change.getPreviousValue("lastName")).isEqualTo("read:lastName");

        final MutableEntityDataChangeSet set = new MutableEntityDataChangeSet();
        set.getUpdated().add(change);
        final EntityDataChange copy = MutableEntityDataChangeSet.clone(set).getUpdated().iterator().next();
        assertThat(copy.getPreviousValue("lastName")).isEqualTo("read:lastName");
    }

    @Test
    public void testCopyResolvesTypedLookupsFromItsOwnChanges()
    {
        final Person person = new Person("Jane", "Doe");
        final EntityDataChange change = new EntityDataChangeImpl(1, person, List.of(new PropertyChange<>("lastName", String.class, "Smith", "Doe")), null);
        final MutableEntityDataChangeSet set = new MutableEntityDataChangeSet();
        set.getUpdated().add(change);
        final EntityDataChange copy = MutableEntityDataChangeSet.clone(set).getUpdated().iterator().next();

        change.getPropertyChanges().clear();

        final Attribute<Person, String> lastName = attribute("lastName");
        assertThat(copy.getPropertyChange(lastName)).hasValueSatisfying(c -> assertThat(c.getOldValue()).isEqualTo("Smith"));
        assertThat(copy.getPreviousValue(lastName)).isEqualTo("Smith");
    }

    @SuppressWarnings("unchecked")
    private static <T> Attribute<Person, T> attribute(String name)
    {
        return (Attribute<Person, T>) Proxy.newProxyInstance(ModelTest.class.getClassLoader(), new Class<?>[]{Attribute.class}, (proxy, method, args) ->
        {
            if (method.getName().equals("getName"))
            {
                return name;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    @SuppressWarnings("unused")
    static class Person
    {
        private final String firstName;
        private final String lastName;

        Person(String firstName, String lastName)
        {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}
