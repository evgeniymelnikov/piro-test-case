package com.github.evgeniymelnikov.piro.model.metamodel;

import com.github.evgeniymelnikov.piro.model.Widget;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Приводятся наименования полей, будет использоваться при проверке в сортировке.
 * Контракт следующий: изменение никаких полей (включая содержание коллекции и массива) недопустимо.
 * Без @Component (с сопутствующей Bean initialization логикой) статический блок будет выполнен только в момент первого обращения к классу,
 * в данном случае лучше, если в момент подъёма приложения будет осуществлена проверка.
 */

@Component
public class Widget_ {

    public final static String ID = "id";
    public final static String POSITION_X = "positionX";
    public final static String POSITION_Y = "positionY";
    public final static String WIDTH = "width";
    public final static String HEIGHT = "height";
    public final static String INDEX_Z = "indexZ";
    public final static String LAST_UPDATE = "lastUpdate";
    public final static String LOCK = "lock";

    private final static String[] fieldNames
            = new String[] {ID, POSITION_X, POSITION_Y, WIDTH, HEIGHT, INDEX_Z, LAST_UPDATE, LOCK};

    static {
        if (!Arrays.asList(fieldNames).containsAll(
                Arrays.stream(Widget.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList()))) {
            throw new RuntimeException("Ошибка, метамодель не актуальна");
        }
    }

    public static String[] getFieldNames() {
        return Arrays.copyOf(fieldNames, fieldNames.length);
    }
}
