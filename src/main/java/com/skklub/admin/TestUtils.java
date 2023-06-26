package com.skklub.admin;

import java.lang.reflect.Field;

public interface TestUtils {
     public static <T> void setIdReflection(Long idVal, T obj) throws Exception {
        Field logoIdField = obj.getClass().getDeclaredField("id");
        logoIdField.setAccessible(true);
        logoIdField.set(obj, idVal);
    }

}
