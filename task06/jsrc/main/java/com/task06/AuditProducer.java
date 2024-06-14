package com.task06;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
		for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
			switch (record.getEventName()) {
				case "INSERT":
					auditInsert(context, record);
					break;
				case "MODIFY":
					auditModify(context, record);
					break;
				default:
					context.getLogger().log("ERROR: Unknown event " + record.getEventName());
			}
		}
		return null;
	}

	private void auditInsert(Context context, DynamodbEvent.DynamodbStreamRecord record) {
		Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
		String id = UUID.randomUUID().toString();
		Item item = new Item()
				.withPrimaryKey("id", id)
				.withString("itemKey", newImage.get("key").getS())
				.withString("modificationTime", new DateTime().toString())
				.withMap("newValue", Map.of(
						"key", newImage.get("key").getS(),
						"value", newImage.get("value").getN()
				));
		context.getLogger().log("item: " + item);
		getTargetTable().putItem(item);
	}

	private void auditModify(Context context, DynamodbEvent.DynamodbStreamRecord record) {
		Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();
		Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
		String id = UUID.randomUUID().toString();
		Item item = new Item()
				.withPrimaryKey("id", id)
				.withString("itemKey", newImage.get("key").getS())
				.withString("modificationTime", new DateTime().toString())
				.withString("updatedAttribute", "value")
				.withMap("oldValue", Map.of(
						"key", oldImage.get("key").getS(),
						"value", oldImage.get("value").getN()
				))
				.withMap("newValue", Map.of(
						"key", newImage.get("key").getS(),
						"value", newImage.get("value").getN()
				));
		context.getLogger().log("item: " + item);
		getTargetTable().putItem(item);
	}

	private Table getTargetTable() {
		return dynamoDB.getTable(System.getenv("target_table"));
	}
}
