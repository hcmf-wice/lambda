package com.task10;

import com.amazonaws.util.StringUtils;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.util.Map;
import java.util.regex.Pattern;

import static com.task10.Util.isValidEmailAddress;

class Validator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[\\w$%^*]+$");

    static void validateSignupRequest(Map<String, String> requestMap) {
        if (!StringUtils.isNullOrEmpty(requestMap.get("firstName"))
                && !StringUtils.isNullOrEmpty(requestMap.get("lastName"))
                && isValidEmailAddress(requestMap.get("email"))
                && !StringUtils.isNullOrEmpty(requestMap.get("password"))
                && (requestMap.get("password")).length() >= 12
                && Pattern.compile("[A-Z]").matcher(requestMap.get("password")).find()
                && Pattern.compile("[a-z]").matcher(requestMap.get("password")).find()
                && Pattern.compile("[0-9]").matcher(requestMap.get("password")).find()
                && Pattern.compile("[$%^*_-]").matcher(requestMap.get("password")).find()
        ) {
            return;
        }

        throw new InvalidRequest();
    }

    static void validateSigninRequest(Map<String, String> requestMap) {
        if (isValidEmailAddress(requestMap.get("email"))
                && !StringUtils.isNullOrEmpty(requestMap.get("password"))
                && (requestMap.get("password")).length() >= 12
                && PASSWORD_PATTERN.matcher(requestMap.get("password")).matches()
        ) {
            return;
        }

        throw new InvalidRequest();
    }

    static void validatePostTablesRequest(Table request) {
        if (request.getId() != null
                && request.getNumber() != null
                && request.getPlaces() != null
                && request.isVip() != null
        ) {
            return;
        }

        throw new InvalidRequest();
    }

    public static void validatePostReservationsRequest(Reservation request) {
        if (request.getTableNumber() != null
                && request.getClientName() != null
                && request.getPhoneNumber() != null
                && request.getDate() != null
                && request.getSlotTimeStart() != null
                && request.getSlotTimeEnd() != null
        ) {
            return;
        }

        throw new InvalidRequest();
    }
}