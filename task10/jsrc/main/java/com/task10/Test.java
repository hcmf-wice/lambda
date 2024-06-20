package com.task10;

import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        System.out.println(Validator.noOverlap(
                new Reservation(1, "", "",
                        "1234-56-78", "12:00", "15:30"),
                List.of(
                        new Reservation(1, "", "",
                                "1234-56-78", "15:00", "17:00"),
        new Reservation(1, "", "",
                "1234-56-78", "18:00", "19:00")
                )
                ));
    }
}

//signup request: {firstName=cmtr-95209e6a-User, lastName=cmtr-95209e6a-Validation, email=cmtr-95209e6a-validation_user@test.com, password=p12345T-048_Gru}