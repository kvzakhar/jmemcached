package net.simplesoft;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class TestUtils {

    public static void setLoggerViaReflection(Class<?> clazz, Logger logger) throws IllegalAccessException {
        Field loggerField = FieldUtils.getField(clazz, "LOGGER", true);
        FieldUtils.removeFinalModifier(loggerField);
        loggerField.set(clazz, logger);
    }
}
