package com.github.evgeniymelnikov.piro.model.validator;

import com.github.evgeniymelnikov.piro.model.Widget;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * z-index не валидируется, потому что может быть определён пользователем как null
 * и тогда он будет определён в момент добавления виджета
 */
@Component
public class WidgetValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Widget.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Widget widget = (Widget) o;
        if (widget.getId() == null) {
            errors.rejectValue("id", "id должен быть определён");
        }

        if (widget.getPositionX() == null) {
            errors.rejectValue("positionX", "координата по оси X должна быть определена");
        }

        if (widget.getPositionY() == null) {
            errors.rejectValue("positionY", "координата по оси Y должна быть определена");
        }

        if (widget.getHeight() == null) {
            errors.rejectValue("height", "высота должна быть определена");
        }

        if (widget.getWidth() == null) {
            errors.rejectValue("width", "ширина должна быть определена");
        }

        if (widget.getLastUpdate() == null) {
            errors.rejectValue("lastUpdate", "дата модификации должна быть определена");
        }
    }

}
