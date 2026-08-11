package com.example.admin_service.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.model.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AdminRepositoryTest {

    private static final String ADMIN_ID = "admin1";

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private PaginatedQueryList<Admin> paginatedQueryList;

    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminRepository = new AdminRepository(dynamoDBMapper);
    }

    @Test
    void testSave() {
        Admin admin = new Admin();
        String result = adminRepository.save(admin);

        verify(dynamoDBMapper, times(1)).save(admin);
        assertEquals("Admin Created Successfully", result);
    }

    @Test
    void testFindById() {
        Admin admin = new Admin();
        admin.setAdminId(ADMIN_ID);

        when(dynamoDBMapper.load(Admin.class, ADMIN_ID)).thenReturn(admin);

        Admin result = adminRepository.findById(ADMIN_ID);

        assertEquals(admin, result);
        verify(dynamoDBMapper, times(1)).load(Admin.class, ADMIN_ID);
    }

    @Test
    void testFindByRoleFound() {
        Admin admin = new Admin();
        admin.setAdminId("a1");

        when(paginatedQueryList.isEmpty()).thenReturn(false);
        when(paginatedQueryList.get(0)).thenReturn(admin);

        when(dynamoDBMapper.query(eq(Admin.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryList);

        Admin result = adminRepository.findByRole(AdminRole.SUPER_ADMIN);

        assertNotNull(result);
        assertEquals("a1", result.getAdminId());
    }

    @Test
    void testFindByRoleNotFound() {
        when(paginatedQueryList.isEmpty()).thenReturn(true);
        when(dynamoDBMapper.query(eq(Admin.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryList);

        Admin result = adminRepository.findByRole(AdminRole.COURSE_ADMIN);

        assertNull(result);
    }

    @Test
    void testDeleteAdminExists() {
        Admin admin = new Admin();
        when(dynamoDBMapper.load(Admin.class, "a1")).thenReturn(admin);

        adminRepository.delete("a1");

        verify(dynamoDBMapper, times(1)).delete(admin);
    }

    @Test
    void testDeleteAdminDoesNotExist() {
        when(dynamoDBMapper.load(Admin.class, "a1")).thenReturn(null);

        adminRepository.delete("a1");

        verify(dynamoDBMapper, times(0)).delete(any());
    }
}