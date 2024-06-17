package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.openmeteo.api.OpenMeteoApi;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Map;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		layers = {"sdk-layer"},
		runtime = DeploymentRuntime.JAVA11,
		architecture = Architecture.ARM64,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"libs/open-meteo-api.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private static final int SC_OK = 200;
	private static final int SC_NOT_FOUND = 404;
	private static final int SC_INTERNAL_SERVER_ERROR = 500;
	private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		context.getLogger().log(requestEvent.toString());
		if ("GET".equals(getMethod(requestEvent)) && "/weather".equals(getPath(requestEvent))) {
			return handleGetWeather();
		} else {
			return notFoundResponse(requestEvent);
		}
	}
	private APIGatewayV2HTTPResponse handleGetWeather() {
		String weather = OpenMeteoApi.getWeatherForecast();
		if (weather != null) {
			return buildResponse(SC_OK, weather);
		} else {
			return buildResponse(SC_INTERNAL_SERVER_ERROR, "Could not get weather!");
		}
	}

	private APIGatewayV2HTTPResponse notFoundResponse(APIGatewayV2HTTPEvent requestEvent) {
		return buildResponse(SC_NOT_FOUND,
						"The resource with method " + getMethod(requestEvent) +
						" and path " + getPath(requestEvent) + " is not found"
		);
	}

	private APIGatewayV2HTTPResponse buildResponse(int statusCode, String body) {
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withHeaders(responseHeaders)
				.withBody(body)
				.build();
	}

	private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getMethod();
	}

	private String getPath(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getPath();
	}
}
