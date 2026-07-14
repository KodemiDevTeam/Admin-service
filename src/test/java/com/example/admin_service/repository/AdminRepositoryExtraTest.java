package com.example.admin_service.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.model.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminRepositoryExtraTest {

    private DynamoDBMapper dynamoDBMapper;
    private AdminRepository adminRepository;

    @BeforeEach
    void setup() {
        dynamoDBMapper = mock(DynamoDBMapper.class);
        adminRepository = new AdminRepository(dynamoDBMapper);
    }


    @Test
    void save_callsDynamoDBMapperSave() {

        Admin admin = new Admin();
        admin.setAdminId("a1");

        String result = adminRepository.save(admin);

        verify(dynamoDBMapper, times(1)).save(admin);
        assertEquals("Admin Created Successfully", result);
    }


    @Test
    void findById_returnsAdmin() {

        Admin admin = new Admin();

        when(dynamoDBMapper.load(Admin.class, "a1"))
                .thenReturn(admin);

        Admin result = adminRepository.findById("a1");

        assertNotNull(result);
        assertSame(admin, result);
    }


    @Test
    void findById_notFound_returnsNull() {

        when(dynamoDBMapper.load(Admin.class, "missing"))
                .thenReturn(null);

        Admin result = adminRepository.findById("missing");

        assertNull(result);
    }


    @Test
    void findByRole_returnsAdmin() {

        Admin admin = new Admin();

        PaginatedQueryList<Admin> queryResult =
                mock(PaginatedQueryList.class);

        when(queryResult.isEmpty())
                .thenReturn(false);

        when(queryResult.get(0))
                .thenReturn(admin);


        when(dynamoDBMapper.query(
                eq(Admin.class),
                any(DynamoDBQueryExpression.class)))
                .thenReturn(queryResult);


        Admin result =
                adminRepository.findByRole(AdminRole.SUPER_ADMIN);


        assertSame(admin, result);
    }


    @Test
    void findByRole_emptyResults_returnsNull() {

        PaginatedQueryList<Admin> queryResult =
                mock(PaginatedQueryList.class);

        when(queryResult.isEmpty())
                .thenReturn(true);


        when(dynamoDBMapper.query(
                eq(Admin.class),
                any(DynamoDBQueryExpression.class)))
                .thenReturn(queryResult);


        Admin result =
                adminRepository.findByRole(AdminRole.USER_ADMIN);


        assertNull(result);
    }


    @Test
    void findByEmail_returnsAdmin() {

        Admin admin = new Admin();

        PaginatedQueryList<Admin> queryResult =
                mock(PaginatedQueryList.class);


        when(queryResult.isEmpty())
                .thenReturn(false);

        when(queryResult.get(0))
                .thenReturn(admin);


        when(dynamoDBMapper.query(
                eq(Admin.class),
                any(DynamoDBQueryExpression.class)))
                .thenReturn(queryResult);


        Admin result =
                adminRepository.findByEmail("user@test.com");


        assertSame(admin, result);
    }


    @Test
    void findByEmail_emptyResults_returnsNull() {

        PaginatedQueryList<Admin> queryResult =
                mock(PaginatedQueryList.class);


        when(queryResult.isEmpty())
                .thenReturn(true);


        when(dynamoDBMapper.query(
                eq(Admin.class),
                any(DynamoDBQueryExpression.class)))
                .thenReturn(queryResult);


        Admin result =
                adminRepository.findByEmail("nobody@test.com");


        assertNull(result);
    }


    @Test
    void delete_adminExists_callsDelete() {

        Admin admin = new Admin();

        when(dynamoDBMapper.load(Admin.class, "a1"))
                .thenReturn(admin);


        adminRepository.delete("a1");


        verify(dynamoDBMapper)
                .delete(admin);
    }


    @Test
    void delete_adminNotFound_doesNotCallDelete() {

        when(dynamoDBMapper.load(Admin.class, "missing"))
                .thenReturn(null);


        adminRepository.delete("missing");


        verify(dynamoDBMapper, never())
                .delete(any(Admin.class));
    }
}