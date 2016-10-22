package space.wsq.kit.devkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings({"unchecked", "unused"})
public class Reflection {
    public static <T> T callMethod(Object target, String method, Class<?>[] parameterTypes, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = target.getClass().getDeclaredMethod(method, parameterTypes);
        m.setAccessible(true);
        Object result = m.invoke(target, args);
        return (T) result;
    }

    public static ReflectionMethod getMethod(Class<?> targetClass, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method m = targetClass.getDeclaredMethod(name, parameterTypes);
        m.setAccessible(true);
        return new ReflectionMethod(m);
    }

    public static <T> T getField(Object target, String property) throws IllegalAccessException, NoSuchFieldException {
        Field localField = findField(target, property);
        localField.setAccessible(true);
        return (T) localField.get(target);
    }

    public static <T> T getField(Object target, Class<?> targetClass, String property) throws IllegalAccessException, NoSuchFieldException {
        Field localField = targetClass.getDeclaredField(property);
        localField.setAccessible(true);
        return (T) localField.get(target);
    }

    public static void setField(Object target, String property, Object value) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = target.getClass();
        Field localField = clazz.getDeclaredField(property);
        localField.setAccessible(true);
        localField.set(target, value);
    }

    public static Field findField(Object target, String property) throws IllegalAccessException, NoSuchFieldException {
        Field f = null;
        Class<?> clazz = target.getClass();
        do {
            try {
                f = clazz.getDeclaredField(property);
            } catch (NoSuchFieldException e) {
                // ignore this.
            }
            clazz = clazz.getSuperclass();
        }
        while (f == null && clazz != Object.class);
        if (f == null) {
            throw new NoSuchFieldException("Field " + property + " not found in current class or super classes.");
        }
        return f;
    }

    public static class ReflectionMethod {
        private final Method proxy;

        public ReflectionMethod(Method proxy) {
            this.proxy = proxy;
        }

        public Object invoke(Object receiver, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return proxy.invoke(receiver, args);
        }
    }
}
