package com.example.admin_service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.example.admin_service.enums.AdminRole;
import lombok.*;

@DynamoDBTable(tableName = "Admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @DynamoDBHashKey(attributeName = "adminId")
    private String adminId;
    @DynamoDBAttribute
    private String password;
    @DynamoDBAttribute
    private String email;
    @DynamoDBAttribute
    private String username;
    @DynamoDBAttribute
    private String userId;
    @DynamoDBAttribute
    private AdminRole adminRole;

}
