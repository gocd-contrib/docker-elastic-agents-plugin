package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.RequestExecutor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetCapabilitiesExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static final Map<String, Boolean> CAPABILITIES_RESPONSE = new LinkedHashMap<>();

    static {
        CAPABILITIES_RESPONSE.put("supports_plugin_status_report", false);
        CAPABILITIES_RESPONSE.put("supports_agent_status_report", true);
        CAPABILITIES_RESPONSE.put("supports_cluster_status_report", true);
    }

    @Override
    public GoPluginApiResponse execute() {
        return DefaultGoPluginApiResponse.success(GSON.toJson(CAPABILITIES_RESPONSE));
    }
}
