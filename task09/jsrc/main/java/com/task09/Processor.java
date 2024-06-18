package com.task09;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "processor",
		roleName = "processor-role",
		runtime = DeploymentRuntime.JAVA11,
		isPublishVersion = false,
		tracingMode = TracingMode.Active,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(
		name = "Weather",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(
		value = {
				@EnvironmentVariable(
						key = "target_table",
						value = "${target_table}"
				)
		}
)
public class Processor implements RequestHandler<Object, Void> {
	private static final String URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
	private static final Regions REGION = Regions.EU_CENTRAL_1;

	private static final Gson gson = new Gson();

	private final DynamoDB dynamoDB;

	public Processor() {
		var dynamoClient = AmazonDynamoDBClientBuilder.standard()
				.withRegion(REGION)
				.build();
		dynamoDB = new DynamoDB(dynamoClient);
	}

	public Void handleRequest(Object request, Context context) {
		var logger = context.getLogger();
		logger.log("Processor called.");
		try {
			var weatherRequest = HttpRequest.newBuilder()
					.uri(new URI(URL))
					.version(HttpClient.Version.HTTP_2)
					.timeout(Duration.of(10L, ChronoUnit.SECONDS))
					.GET()
					.build();
			var response = HttpClient.newHttpClient().send(weatherRequest, HttpResponse.BodyHandlers.ofString());

			var id = UUID.randomUUID().toString();
			var forecast = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>(){});
			forecast.remove("current_units");
			forecast.remove("current");
			((Map<String, Object>) forecast.get("hourly_units")).remove("relative_humidity_2m");
			((Map<String, Object>) forecast.get("hourly_units")).remove("wind_speed_10m");
			((Map<String, Object>) forecast.get("hourly")).remove("relative_humidity_2m");
			((Map<String, Object>) forecast.get("hourly")).remove("wind_speed_10m");
			var item = new Item()
					.withPrimaryKey("id", id)
					.withMap("forecast", forecast);
			var outcome = getTargetTable().putItem(item);
			logger.log(outcome.toString());
		} catch (IOException | InterruptedException | URISyntaxException ex) {
			logger.log(ex.getStackTrace().toString());
		}
		return null;
	}

	private Table getTargetTable() {
		return dynamoDB.getTable(System.getenv("target_table"));
	}
}
