package com.task10;

import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Validator.validateSignupRequest(Map.of(
                "firstName", "cmtr-95209e6a-User",
                "lastName", "cmtr-95209e6a-Validation",
                "email", "cmtr-95209e6a-validation_user@test.com",
                "password", "p12345T-048_Gru"
        ));

        System.out.println(Util.isValidEmailAddress("cmtr-95209e6a-validation_user@test.com"));
    }
}

//signup request: {firstName=cmtr-95209e6a-User, lastName=cmtr-95209e6a-Validation, email=cmtr-95209e6a-validation_user@test.com, password=p12345T-048_Gru}