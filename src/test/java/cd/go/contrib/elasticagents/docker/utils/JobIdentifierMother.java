package cd.go.contrib.elasticagents.docker.utils;

import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import com.google.gson.JsonObject;

public class JobIdentifierMother {

    public static JsonObject getJson() {
        JsonObject jobIdentifierJson = new JsonObject();
        jobIdentifierJson.addProperty("pipeline_name", "up42");
        jobIdentifierJson.addProperty("pipeline_counter", 1);
        jobIdentifierJson.addProperty("pipeline_label", "label");
        jobIdentifierJson.addProperty("stage_name", "stage");
        jobIdentifierJson.addProperty("stage_counter", "1");
        jobIdentifierJson.addProperty("job_name", "job1");
        jobIdentifierJson.addProperty("job_id", 1);
        return jobIdentifierJson;
    }

    public static JobIdentifier get() {
        return new JobIdentifier("up42", 1L, "label", "stage", "1", "job1", 1L);
    }

}
