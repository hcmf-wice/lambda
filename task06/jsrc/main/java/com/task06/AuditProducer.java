package com.task06;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
		lambdaName = "audit_producer",
		roleName = "audit_producer-role",
		runtime = DeploymentRuntime.JAVA11,
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(
		name = "Configuration",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@DependsOn(
		name = "Audit",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 1
)
@EnvironmentVariables(
		value = {
				@EnvironmentVariable(
						key = "target_table",
						value = "${target_table}"
				)
		}
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
	private static String TARGET_TABLE_NAME = "cmtr-95209e6a-Audit";
	private static Regions REGION = Regions.EU_CENTRAL_1;

	private DynamoDB dynamoDB;

	public AuditProducer() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion(REGION)
				.build();
		dynamoDB = new DynamoDB(client);
	}

	public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
		context.getLogger().log("dynamodbEvent: " + dynamodbEvent.toString());
//		for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {

//		}
		return null;
	}

	private Table getTargetTable() {
		return dynamoDB.getTable(System.getenv("target_table"));
	}
}
