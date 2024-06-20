package com.task11;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Gson gson = new Gson();
        Reservation request = gson.fromJson("{\n" +
                "  \"tableNumber\": 12345,\n" +
                "  \"clientName\": \"qwe\",\n" +
                "  \"phoneNumber\": \"123-4567\",\n" +
                "  \"date\": \"1234-56-78\",\n" +
                "  \"slotTimeStart\": \"13:00\",\n" +
                "  \"slotTimeEnd\": \"15:00\"\n" +
                "}", new TypeToken<>(){});
        System.out.println(request);
    }
}

//signup request: {firstName=cmtr-95209e6a-User, lastName=cmtr-95209e6a-Validation, email=cmtr-95209e6a-validation_user@test.com, password=p12345T-048_Gru}