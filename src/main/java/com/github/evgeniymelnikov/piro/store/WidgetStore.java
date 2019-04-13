package com.github.evgeniymelnikov.piro.store;

import com.github.evgeniymelnikov.piro.model.Widget;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Getter
public class WidgetStore {
    private final CopyOnWriteArrayList<Widget> store = new CopyOnWriteArrayList<>();
}
