package com.task10;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		runtime = DeploymentRuntime.JAVA11,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(
		name = "Tables",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@DependsOn(
		name = "Reservations",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@EnvironmentVariables(
		value = {
				@EnvironmentVariable(key = "tables_table", value = "${tables_table}"),
				@EnvironmentVariable(key = "reservations_table", value = "${reservations_table}"),
				@EnvironmentVariable(key = "booking_userpool", value = "${booking_userpool}")
		}
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.defaultClient();
	private static final Gson gson = new Gson();
	private static final Pattern passwordPattern = Pattern.compile("^[\\w$%^*]+$");

	private final String tablesTable;
	private final String reservationsTable;
	private final String bookingUserpool;

	private LambdaLogger logger;

	public ApiHandler() {
		tablesTable = System.getenv("tables_table");
		reservationsTable = System.getenv("reservations_table");
		bookingUserpool = System.getenv("booking_userpool");
	}

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		logger = context.getLogger();

		String path = requestEvent.getPath();
		switch (path) {
			case "/signup":
				if (List.of("POST").contains(requestEvent.getHttpMethod())) {
					return handleSignup(requestEvent);
				}
			case "/signin":
				if (List.of("POST").contains(requestEvent.getHttpMethod())) {
					return handleSignin(requestEvent);
				}
			case "/tables":
				if (List.of("POST", "GET").contains(requestEvent.getHttpMethod())) {
					return handleTables(requestEvent);
				}
			case "/reservations":
				if (List.of("POST", "GET").contains(requestEvent.getHttpMethod())) {
					return handleReservations(requestEvent);
				}
			default:
				if (path.startsWith("/tables/") && List.of("GET").contains(requestEvent.getHttpMethod())) {
					return handleTablesById(requestEvent);
				}
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_NOT_FOUND);
	}

	private APIGatewayProxyResponseEvent handleSignup(APIGatewayProxyRequestEvent requestEvent) {
		var body = requestEvent.getBody();
		try {
			var requestMap = gson.fromJson(body, new TypeToken<Map<String, String>>(){});
			validateSignupRequest(requestMap);

			SignUpRequest signUpRequest = new SignUpRequest()
					.withUsername(requestMap.get("email"))
					.withPassword(requestMap.get("password"))
					.withClientId(getClientId());
			cognitoClient.signUp(signUpRequest);

			AdminConfirmSignUpRequest adminConfirmSignUpRequest = new AdminConfirmSignUpRequest()
					.withUsername(requestMap.get("email"))
							.withUserPoolId(getUserPoolId());
			cognitoClient.adminConfirmSignUp(adminConfirmSignUpRequest);

			return ok("");
		} catch (Exception ex) {
			logger.log(ex.toString());
			logger.log(Arrays.toString(ex.getStackTrace()));
			return badRequest();
		}
	}

	private void validateSignupRequest(Map<String, String> requestMap) {
		if (!StringUtils.isNullOrEmpty(requestMap.get("firstName"))
				&& !StringUtils.isNullOrEmpty(requestMap.get("lastName"))
				&& isValidEmailAddress(requestMap.get("email"))
				&& !StringUtils.isNullOrEmpty(requestMap.get("password"))
				&& (requestMap.get("password")).length() >= 12
				&& Pattern.compile("[A-Z]").matcher(requestMap.get("password")).find()
				&& Pattern.compile("[a-z]").matcher(requestMap.get("password")).find()
				&& Pattern.compile("[0-9]").matcher(requestMap.get("password")).find()
				&& Pattern.compile("[$%^*]").matcher(requestMap.get("password")).find()
		) {
			return;
		}

		throw new InvalidRequest();
	}

	private APIGatewayProxyResponseEvent handleSignin(APIGatewayProxyRequestEvent requestEvent) {
		var body = requestEvent.getBody();

		try {
			var requestMap = gson.fromJson(body, new TypeToken<Map<String, String>>(){});
			validateSigninRequest(requestMap);

			var authRequest = new AdminInitiateAuthRequest()
					.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
					.withAuthParameters(Map.of(
							"USERNAME", requestMap.get("email"),
							"PASSWORD", requestMap.get("password")
					))
					.withUserPoolId(getUserPoolId())
					.withClientId(getClientId());

			var authResponse = cognitoClient.adminInitiateAuth(authRequest);
			logger.log(authResponse.toString());
			var accessToken = authResponse.getAuthenticationResult().getAccessToken();

			return ok(gson.toJson(Map.of("accessToken", accessToken)));

		} catch (Exception ex) {
			logger.log(ex.toString());
			logger.log(Arrays.toString(ex.getStackTrace()));
			return badRequest();
		}
	}

	private void validateSigninRequest(Map<String, String> requestMap) {
		if (isValidEmailAddress(requestMap.get("email"))
				&& !StringUtils.isNullOrEmpty(requestMap.get("password"))
				&& (requestMap.get("password")).length() >= 12
				&& passwordPattern.matcher(requestMap.get("password")).matches()
		) {
			return;
		}

		throw new InvalidRequest();
	}

	private APIGatewayProxyResponseEvent handleTables(APIGatewayProxyRequestEvent requestEvent) {
		return null;
	}

	private APIGatewayProxyResponseEvent handleReservations(APIGatewayProxyRequestEvent requestEvent) {
		return null;
	}

	private APIGatewayProxyResponseEvent handleTablesById(APIGatewayProxyRequestEvent requestEvent) {
		return null;
	}

	private APIGatewayProxyResponseEvent badRequest() {
		return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
	}

	private APIGatewayProxyResponseEvent ok(String message) {
		return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK).withBody(message);
	}

	public class InvalidRequest extends RuntimeException {
	}

	private static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	private String getUserPoolId() {
		return cognitoClient.listUserPools(new ListUserPoolsRequest().withMaxResults(10))
				.getUserPools().stream()
				.filter(pool -> pool.getName().contains(bookingUserpool))
				.findAny()
				.orElseThrow(() -> new RuntimeException(String.format("User pool %s not found.", bookingUserpool)))
				.getId();
	}

	private String getClientId() {
		return cognitoClient.listUserPoolClients(new ListUserPoolClientsRequest().withUserPoolId(getUserPoolId()).withMaxResults(1))
				.getUserPoolClients().stream()
				.filter(client -> client.getClientName().contains("client-app"))
				.findAny()
				.orElseThrow(() -> new RuntimeException(String.format("Client 'client-app' not found.", bookingUserpool)))
				.getClientId();
	}

//	public record Signup(
//			String firstName,
//			String lastName,
//			String email,
//			String password
//	) {}
//
//	public record Signin(
//			String email,
//			String password
//	) {}
//
//	public record SigninResponse(
//			String accessToken
//	) {}
//
//	public record Tables(
//			List<Table> tables
//	) {}
//
//	public record Table(
//			int id,
//			int number,
//			int places,
//			boolean isVip,
//			Integer minOrder
//	) {}
//
//	public record TableResponse(
//			int id
//	) {}
//
//	public record Reservation(
//			int tableNumber,
//			String clientName,
//			String phoneNumber,
//			String date,
//			String slotTimeStart,
//			String slotTimeEnd
//	) {}
//
//	public record ReservationResponse(
//			String reservationId
//	) {}
}
