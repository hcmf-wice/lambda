package com.task05;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		runtime = DeploymentRuntime.JAVA11,
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<ApiHandler.Request, ApiHandler.Response> {
	private static String DYNAMODB_TABLE_NAME = "Events";
	private static Regions REGION = Regions.EU_CENTRAL_1;

	public Response handleRequest(Request request, Context context) {
		context.getLogger().log("request: " + request.toString());

		Table table = getEventsTable();
		Item item = new Item()
				.withPrimaryKey("id", UUID.randomUUID())
				.withInt("principalId", request.getPrincipalId())
				.withString("createdAt", new DateTime().toString())
				.withMap("body", request.content);
		context.getLogger().log("item: " + item);

		PutItemOutcome putItemOutcome = table.putItem(item);
		context.getLogger().log("putItemOutcome: " + putItemOutcome.toString());

		Response response = new Response();
		response.setStatusCode(200);
		response.setEvent(item);
		return response;
	}

	private Table getEventsTable() {
		return new DynamoDB(AmazonDynamoDBClientBuilder.standard()
				.withRegion(REGION)
				.build()).getTable(DYNAMODB_TABLE_NAME);
	}

	public static class Request {
		private int principalId;
		private Map<String, String> content;

		public int getPrincipalId() {
			return principalId;
		}

		public void setPrincipalId(int principalId) {
			this.principalId = principalId;
		}

		public Map<String, String> getContent() {
			return content;
		}

		public void setContent(Map<String, String> content) {
			this.content = content;
		}
	}

	public static class Response {
		private int statusCode;
		private Object event;

		public int getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		public Object getEvent() {
			return event;
		}

		public void setEvent(Object event) {
			this.event = event;
		}
	}

	public static class Event {
		private String id;
		private int principalId;
		private String createdAt;
		private Map<String, String> body;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public int getPrincipalId() {
			return principalId;
		}

		public void setPrincipalId(int principalId) {
			this.principalId = principalId;
		}

		public String getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(String createdAt) {
			this.createdAt = createdAt;
		}

		public Map<String, String> getBody() {
			return body;
		}

		public void setBody(Map<String, String> body) {
			this.body = body;
		}
	}
}
