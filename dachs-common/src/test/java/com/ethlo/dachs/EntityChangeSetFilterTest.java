package com.ethlo.dachs;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.util.StringUtils;

public class EntityChangeSetFilterTest
{
    @Test
    public void testFiltering()
    {
        final CollectingEntityChangeSetListener listener = new CollectingEntityChangeSetListener();
        final DefaultFilteredEntityChangeSetListener filter = new DefaultFilteredEntityChangeSetListener(listener, 
                        c->{return c.getClass().getAnnotation(Deprecated.class) == null;}, 
                        ch->{return ch.getKey().getAnnotation(Deprecated.class) == null;});
        final MutableEntityDataChangeSet cs = new MutableEntityDataChangeSet();
        cs.getCreated().add(new EntityDataChangeImpl(123, new DeprecatedEntityClass(), Arrays.asList(new PropertyChange<Integer>("id", Integer.class, null, 123))));
        filter.preDataChanged(cs);
        System.out.println(StringUtils.collectionToCommaDelimitedString(listener.getPreDataChangeSet().getCreated()));
    }
    
    @EntityListenerIgnore
    private class DeprecatedEntityClass
    {
        @SuppressWarnings("unused")
        private int id = 123;
    }
}
