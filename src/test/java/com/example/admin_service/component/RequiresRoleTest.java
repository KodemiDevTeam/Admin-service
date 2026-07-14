package com.example.admin_service.component;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RequiresRoleTest {

    static class TestClass {

        @RequiresRole({"ADMIN", "MANAGER"})
        public void testMethod() {
        }
    }

    @Test
    void testRequiresRoleAnnotation() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");

        assertTrue(method.isAnnotationPresent(RequiresRole.class));

        RequiresRole annotation = method.getAnnotation(RequiresRole.class);

        assertNotNull(annotation);
        assertArrayEquals(
                new String[]{"ADMIN", "MANAGER"},
                annotation.value()
        );
    }
}