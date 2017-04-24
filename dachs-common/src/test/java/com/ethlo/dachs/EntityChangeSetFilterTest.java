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
        final EntityChangeSetListenerFilter filter = new EntityChangeSetListenerFilter(listener, 
                        c->{return c.getClass().getAnnotation(Deprecated.class) == null;}, 
                        ch->{return !ch.hasAnnotation(Deprecated.class);});
        final MutableEntityDataChangeSet cs = new MutableEntityDataChangeSet();
        final EntityListenerIgnore[] annotations = new EntityListenerIgnore[]
        {
            new EntityListenerIgnore()
            {
                @Override
                public Class<? extends java.lang.annotation.Annotation> annotationType()
                {
                    return Deprecated.class;
                }
            }
        };
        
        cs.getCreated().add(new EntityDataChangeImpl(123, new DeprecatedEntityClass(), Arrays.asList(new PropertyChange<Integer>("id", Integer.class, null, 123, annotations))));
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
