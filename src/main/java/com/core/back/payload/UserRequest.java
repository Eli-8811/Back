package com.core.back.payload;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    private Long userId;
    @NotBlank(message = "First name is required.")
    private String firstName;
    @NotBlank(message = "Last name is required.")
    private String lastName;
    private Integer age;
    private Integer gender;
    @NotBlank(message = "Username is required.")
    private String username;
    @NotBlank(message = "Password is required.")
    private String password;
    @Email(message = "Email must be valid.")
    @NotBlank(message = "Email is required.")
    private String email;
    private Boolean active;
    private List<Long> roleIds;

    public UserRequest() {
    }

    public UserRequest(Long userId, String firstName, String lastName, Integer age, Integer gender,
                       String username, String password, String email, Boolean active, List<Long> roleIds) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = active;
        this.roleIds = roleIds;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

}
