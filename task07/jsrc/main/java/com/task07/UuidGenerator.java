package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import org.joda.time.DateTime;

import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(
		targetRule = "uuid_trigger"
)
@DependsOn(
		name = "uuid-storage",
		resourceType = ResourceType.S3_BUCKET
)
@EnvironmentVariables(
		value = {
				@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
		}
)
@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
public class UuidGenerator implements RequestHandler<ScheduledEvent, Void> {
	private static AmazonS3 s3 = new AmazonS3Client();
	private static Gson gson = new Gson();

	public Void handleRequest(ScheduledEvent event, Context context) {
		context.getLogger().log("event: " + event);
		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			uuids.add(UUID.randomUUID().toString());
		}
		String targetBucket = System.getenv("target_bucket");
		String fileName = new DateTime().toString();
		String file = gson.toJson(Map.of(
				"ids", uuids
		));
		context.getLogger().log("targetBucket: " + targetBucket);
		context.getLogger().log("fileName: " + fileName);
		context.getLogger().log("file: " + file);
		s3.putObject(
				targetBucket,
				fileName,
				file
		);
		return null;
	}
}
