package com.github.evgeniymelnikov.piro.model.filter;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.model.Widget;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.function.Predicate;

public class WidgetFilterTest {

    @Test(expected = ResourceIllegalArgumentException.class)
    public void notCorrectSortFieldNameFailTest(){
        String notCorrectSortFieldName = "string";
        new WidgetFilter(null,null, notCorrectSortFieldName, null, null, null)
                .checkSortFieldsExists();
    }

    @Test
    public void correctSortNotDefaultFieldNameTest(){
        String correctFieldName = "positionX";
        WidgetFilter widgetFilter = new WidgetFilter(null, null, correctFieldName, null, null, null);
        widgetFilter.checkSortFieldsExists();
        MatcherAssert.assertThat(Arrays.asList(widgetFilter.getSortFields()), Matchers.contains("id", correctFieldName));
    }

    @Test
    public void defaultSortFieldTest(){
        WidgetFilter widgetFilter = new WidgetFilter(null, null, null, null, null, null);
        // в данном случае (в конструктор в качестве параметра sort передан null), поле по умочанию - indexZ
        String sortField = widgetFilter.getSort();
        widgetFilter.checkSortFieldsExists();
        MatcherAssert.assertThat(Arrays.asList(widgetFilter.getSortFields()), Matchers.contains("id", sortField));

    }

    @Test
    public void toPredicate() {
        WidgetFilter widgetFilter = new WidgetFilter(null, null, null, null,
                new Point(5L, 5L), new Point(10L, 10L));
        Predicate<Widget> widgetPredicate = widgetFilter.toPredicate();
        Assert.assertNotNull(widgetPredicate);
        Widget widgetMock = Mockito.mock(Widget.class);
        // виджет попадает в пределы фильтра
        Mockito.when(widgetMock.getPositionX()).thenReturn(6L);
        Mockito.when(widgetMock.getPositionY()).thenReturn(6L);
        MatcherAssert.assertThat(widgetPredicate.test(widgetMock), Matchers.equalTo(true));
    }

    @Test
    public void toPredicateWidgetDoesNotMatchPointFilters() {
        WidgetFilter widgetFilter = new WidgetFilter(null, null, null, null,
                new Point(5L, 5L), new Point(10L, 10L));
        Predicate<Widget> widgetPredicate = widgetFilter.toPredicate();
        Assert.assertNotNull(widgetPredicate);
        Widget widgetMock = Mockito.mock(Widget.class);
        // виджет не попадает в пределы фильтра
        Mockito.when(widgetMock.getPositionX()).thenReturn(3L);
        Mockito.when(widgetMock.getPositionY()).thenReturn(3L);
        MatcherAssert.assertThat(widgetPredicate.test(widgetMock), Matchers.equalTo(false));
    }

    @Test
    public void toPredicateWithNullFilters() {
        WidgetFilter widgetFilter = new WidgetFilter(null, null, null, null,null, null);
        Predicate<Widget> widgetPredicate = widgetFilter.toPredicate();
        Assert.assertNotNull(widgetPredicate);
        Widget widgetMock = Mockito.mock(Widget.class);
        Mockito.when(widgetMock.getPositionX()).thenReturn(6L);
        Mockito.when(widgetMock.getPositionY()).thenReturn(6L);

        Assert.assertTrue(widgetPredicate.test(widgetMock));
        Assert.assertTrue(widgetPredicate.test(null));
    }
}