package com.example.admin_service.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.admin_service.model.Admin;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public AdminRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public String save(Admin admin) {
        dynamoDBMapper.save(admin);
        return "Admin Created Successfully";
    }

    public Admin findById(String adminId) {
        return dynamoDBMapper.load(Admin.class, adminId);
    }

    public void delete(String adminId) {
        Admin admin = dynamoDBMapper.load(Admin.class, adminId);
        if(admin != null){
            dynamoDBMapper.delete(admin);
        }
    }
}