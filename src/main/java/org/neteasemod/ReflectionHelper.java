//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.neteasemod;

import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ReflectionHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final boolean Debug = false;
    public static final boolean Studio = false;
    public static UserPropertiesEx userPropertiesEx = null;

    public ReflectionHelper() {
    }

    public static @Nullable Object GetField(Class targetClass, Object target, String fieldName) {
        try {
            Field[] field = targetClass.getDeclaredFields();

            for(int j = 0; j < field.length; ++j) {
                String name = field[j].getName();
                if (fieldName.isEmpty() || name.equals(fieldName)) {
                    field[j].setAccessible(true);
                    Object obj = field[j].get(target);
                    return obj;
                }
            }
        } catch (Exception e) {
            LOGGER.error("GetField", e);
        }

        return null;
    }

    public static Object GetField(Class targetClass, Object target, Type fieldType) {
        try {
            Field[] field = targetClass.getDeclaredFields();

            for(int j = 0; j < field.length; ++j) {
                field[j].setAccessible(true);
                Object obj = field[j].get(target);
                if (obj.getClass() == fieldType) {
                    return obj;
                }
            }
        } catch (Exception e) {
            LOGGER.error("GetField", e);
        }

        return null;
    }

    public static void SetField(Class targetClass, Object target, String fieldName, Object value) {
        try {
            Field[] field = targetClass.getDeclaredFields();

            for(int j = 0; j < field.length; ++j) {
                String name = field[j].getName();
                if (fieldName.isEmpty() || name.equals(fieldName)) {
                    field[j].setAccessible(true);
                    field[j].set(target, value);
                }
            }
        } catch (Exception e) {
            LOGGER.error("SetField", e);
        }

    }
    // 获取字段值
    public static Object getField(Class<?> clazz, Object instance, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    // 设置字段值
    public static void setField(Class<?> clazz, Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    // 调用私有方法
    public static Object invokeMethod(Class<?> clazz, Object instance, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        try {
            return method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static Method GetMethod(Class targetClass, String name) {
        try {
            Method[] methods = targetClass.getDeclaredMethods();

            for(int j = 0; j < methods.length; ++j) {
                methods[j].setAccessible(true);
                if (methods[j].getName().equals(name)) {
                    return methods[j];
                }
            }
        } catch (Exception e) {
            LOGGER.error("GetField", e);
        }

        return null;
    }

    public static Object Invoke(Object target, String name, Object... args) {
        try {
            Method method = GetMethod(target.getClass(), name);
            if (null == method) {
                LOGGER.error(String.format("Method %s not found in class %s", name, target.getClass().toString()));
                return null;
            } else {
                return method.invoke(target, args);
            }
        } catch (Exception e) {
            LOGGER.error("Invoke", e);
            return null;
        }
    }

    public static String[] GetCmdArgs() {
        try {
            Class<?> fmlLoader = Class.forName("net.neoforged.fml.loading.FMLLoader");
            Field f = fmlLoader.getField("programArguments");
            return (String[])f.get((Object)null);
        } catch (Exception var2) {
            return null;
        }
    }

    public static String GetChannel() {
        if (userPropertiesEx == null) {
            String[] args = GetCmdArgs();
            if (args != null) {
                for(int i = 0; i < args.length; ++i) {
                    if (args[i].equals("--userPropertiesEx")) {
                        Gson gson = new Gson();
                        userPropertiesEx = (UserPropertiesEx)gson.fromJson(args[i + 1], UserPropertiesEx.class);
                        break;
                    }
                }
            }

            if (userPropertiesEx == null) {
                return "netease";
            }
        }

        return userPropertiesEx.channel;
    }

    public class UserPropertiesEx {
        public int GameType = 0;
        public boolean isFilter = false;
        public String channel = "netease";

        public UserPropertiesEx() {
        }
    }
}
