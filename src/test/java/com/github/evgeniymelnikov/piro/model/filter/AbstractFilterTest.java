package com.github.evgeniymelnikov.piro.model.filter;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

public class AbstractFilterTest {



    @Test
    public void checkSortFieldsExists() {
        AbstractFilter abstractFilterMock = Mockito.mock(AbstractFilter.class);
        Mockito.when(abstractFilterMock.getSortFields()).thenReturn(new String[]{"positionX", "id"});
        Mockito.when(abstractFilterMock.getFieldsFromMetamodel()).thenReturn(new String[]{"width", "indexZ","positionX", "id"});
        Mockito.doCallRealMethod().when(abstractFilterMock).checkSortFieldsExists();
        abstractFilterMock.checkSortFieldsExists();
    }

    @Test(expected = ResourceIllegalArgumentException.class)
    public void checkSortFieldsExistsFailTest() {
        AbstractFilter abstractFilterMock = Mockito.mock(AbstractFilter.class);
        Mockito.when(abstractFilterMock.getSortFields()).thenReturn(new String[]{"dsfjsdjklfksdjfhk", "id"});
        Mockito.when(abstractFilterMock.getFieldsFromMetamodel()).thenReturn(new String[]{"width", "indexZ","positionX", "id"});
        Mockito.doCallRealMethod().when(abstractFilterMock).checkSortFieldsExists();
        abstractFilterMock.checkSortFieldsExists();
    }

    @Test
    public void toPageable() {
        AbstractFilter abstractFilterMock = Mockito.mock(AbstractFilter.class);
        ReflectionTestUtils.setField(abstractFilterMock, "page", 0L);
        ReflectionTestUtils.setField(abstractFilterMock, "limit", 100L);
        ReflectionTestUtils.setField(abstractFilterMock, "direction", SortDirection.ASC);
        Mockito.when(abstractFilterMock.getSortFields()).thenReturn(new String[]{"someName", "id"});
        Mockito.doNothing().when(abstractFilterMock).checkSortFieldsExists();
        Mockito.doCallRealMethod().when(abstractFilterMock).toPageable();
        Pageable pageable = abstractFilterMock.toPageable();
        Assert.assertEquals((Long) 0L, pageable.getPage());
        Assert.assertEquals((Long) 100L, pageable.getLimit());
        Assert.assertEquals(SortDirection.ASC, pageable.getDirection());
        MatcherAssert.assertThat(Arrays.asList(pageable.getSort()), Matchers.contains(abstractFilterMock.getSortFields()));
    }
}