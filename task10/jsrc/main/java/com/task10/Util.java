package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.apache.http.HttpStatus;

public class Util {
    static APIGatewayProxyResponseEvent badRequest() {
        return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
    }

    static APIGatewayProxyResponseEvent ok(String message) {
        return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK).withBody(message);
    }

    static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }
}
