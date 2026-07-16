package io.github.thefakedevs.taczextras.compat;

import com.tacz.guns.entity.shooter.ShooterDataHolder;
import io.github.thefakedevs.taczextras.TaczExtras;

import java.lang.reflect.Field;

public final class ScriptDataCompat {
    private static final Field SCRIPT_DATA = findScriptDataField();

    private ScriptDataCompat() {
    }

    private static Field findScriptDataField() {
        try {
            return ShooterDataHolder.class.getField("scriptData");
        } catch (ReflectiveOperationException exception) {
            TaczExtras.LOGGER.warn("TACZ scriptData field is unavailable; bolt script state will not be preserved");
            return null;
        }
    }

    public static Object read(ShooterDataHolder holder) {
        if (SCRIPT_DATA == null) {
            return null;
        }
        try {
            return SCRIPT_DATA.get(holder);
        } catch (IllegalAccessException exception) {
            TaczExtras.LOGGER.debug("Unable to read TACZ scriptData", exception);
            return null;
        }
    }

    public static void write(ShooterDataHolder holder, Object value) {
        if (SCRIPT_DATA == null) {
            return;
        }
        try {
            SCRIPT_DATA.set(holder, value);
        } catch (IllegalAccessException exception) {
            TaczExtras.LOGGER.debug("Unable to restore TACZ scriptData", exception);
        }
    }
}
