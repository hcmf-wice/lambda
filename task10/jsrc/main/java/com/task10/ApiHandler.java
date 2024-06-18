package com.task10;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.List;

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

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
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
		return new APIGatewayProxyResponseEvent().withStatusCode(404);
	}

	private APIGatewayProxyResponseEvent handleSignup(APIGatewayProxyRequestEvent requestEvent) {
		var body = requestEvent.getBody();


		return null;
	}

	private APIGatewayProxyResponseEvent handleSignin(APIGatewayProxyRequestEvent requestEvent) {
		return null;
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

	public record Signup(
			String firstName,
			String lastName,
			String email,
			String password
	) {}

	public record Signin(
			String email,
			String password
	) {}

	public record SigninResponse(
			String accessToken
	) {}

	public record Tables(
			List<Table> tables
	) {}

	public record Table(
			int id,
			int number,
			int places,
			boolean isVip,
			Integer minOrder
	) {}

	public record TableResponse(
			int id
	) {}

	public record Reservation(
			int tableNumber,
			String clientName,
			String phoneNumber,
			String date,
			String slotTimeStart,
			String slotTimeEnd
	) {}

	public record ReservationResponse(
			String reservationId
	) {}
}
