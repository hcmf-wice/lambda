package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

@LambdaHandler(
		lambdaName = "hello_world",
		roleName = "hello_world-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
		context.getLogger().log(request.toString());
		String path = request.getRequestContext().getHttp().getPath();
		String method = request.getRequestContext().getHttp().getMethod();
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		if ("/hello".equals(path)) {
			response.setStatusCode(200);
			response.setBody(String.format("{\"message\": \"Hello from Lambda\"}"));
		} else {
			response.setStatusCode(400);
			response.setBody(String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", path, method));
		}
		return response;
	}
}
