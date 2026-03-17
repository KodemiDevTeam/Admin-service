package com.example.admin_service.component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresSuperRole {
    String[] value();  // accepts one or more roles e.g. "ADMIN", "MANAGER"
}