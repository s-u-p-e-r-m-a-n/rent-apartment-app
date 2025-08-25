package com.example.auth_module.controller;

public class AuthControllerPath {

    public static final String BASE_URL = "/api/auth";
    public static final String REGISTRATION_NEW_USER = BASE_URL + "/registration";
    public static final String AUTHORIZATION_USER = BASE_URL + "/authorization";
    public static final String ADD_COMMENT = BASE_URL + "/comment";
    public static final String ADMIN_USER = BASE_URL + "/admin";
    public static final String CHANGE_ROLE_USERS = BASE_URL + "/changeusers/{id}";
    public static final String DELETE_USER = ADMIN_USER + "/{id}";

}
