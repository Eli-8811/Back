package com.core.back.entity;

import java.util.List;

public class UserEntity extends BaseEntity {

    private Long userId;
    private String firstName;
    private String lastName;
    private Integer age;
    private Integer gender;
    private String username;
    private String password;
    private String email;

    private List<RoleEntity> roles;

    public UserEntity() {
    }

    public UserEntity(Long userId, String firstName, String lastName, Integer age, Integer gender,
                      String username, String password, String email, List<RoleEntity> roles) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }

}