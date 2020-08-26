package com.gigaspaces.miri;


import com.gigaspaces.miri.dialogs.JenkinsCreateBuildDialog;
import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JenkinsAssistant {

    private static final String key_auth = "miri.jenkins.credentials";

    public static String getCredentials() {
        return PropertiesComponent.getInstance().getValue(key_auth, "");
    }

    public static void setCredentials(String credentials) {
        if (credentials == null || credentials.isEmpty()) {
            PropertiesComponent.getInstance().unsetValue(key_auth);
        } else {
            PropertiesComponent.getInstance().setValue(key_auth, credentials);
        }
    }

    public static List<JenkinsParameter> getJobParameters() throws Exception {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).build())
                .build()) {
            byte[] credentials = Base64.encodeBase64((getCredentials()).getBytes(StandardCharsets.UTF_8));
            HttpGet request = new HttpGet("http://groot.gspaces.com:8080/job/xap-insightedge/job/latest/job/continuous/api/json");
            request.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = client.execute(request)) {

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new Exception("Failed to get job parameters: "+response.getStatusLine().getStatusCode()+"/"+response.getStatusLine().getReasonPhrase());
                }

                Gson gson = new Gson();
                String responseBody = EntityUtils.toString(response.getEntity());
                Map responseData = gson.fromJson(responseBody, Map.class);

                List<Map<String, Object>> actions = (List<Map<String, Object>>) responseData.get("actions");
                Map<String, Object> actionForParameterDefinitions = actions.stream().filter(o -> o.containsKey("parameterDefinitions")).collect(Collectors.toList()).get(0);
                List<Map<String, Object>> parameterDefinitions = (List<Map<String, Object>>) actionForParameterDefinitions.get("parameterDefinitions");

                List<JenkinsParameter> jenkinsParameters = new ArrayList<>();
                parameterDefinitions.forEach(stringObjectMap -> {
                    JenkinsParameter parameter = new JenkinsParameter();
                    parameter.setName(stringObjectMap.get("name").toString());
                    String type = stringObjectMap.get("type").toString();
                    parameter.setType(type.equalsIgnoreCase("BooleanParameterDefinition") ? ParameterType.BOOLEAN : ParameterType.STRING);
                    parameter.setDefaultValue(((Map<String, String> )stringObjectMap.get("defaultParameterValue")).get("value"));
                    jenkinsParameters.add(parameter);
                });


                return jenkinsParameters;

            }
        }
    }


    public static void triggerBuild(List<JenkinsCreateBuildDialog.BuildParameter> buildParameters) throws Exception {

        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(5000).build())
                .build()) {
            byte[] credentials = Base64.encodeBase64((getCredentials()).getBytes(StandardCharsets.UTF_8));

            URIBuilder builder = new URIBuilder("http://groot.gspaces.com:8080/job/xap-insightedge/job/latest/job/continuous/buildWithParameters");
            buildParameters.forEach(param -> builder.setParameter(param.getName(), param.getValue()));

            HttpPost request = new HttpPost(builder.build());
            request.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            try (CloseableHttpResponse res = client.execute(request)) {
                if (res.getStatusLine().getStatusCode() != 201) {
                    throw new Exception("Could not trigger build: " + res.getStatusLine().getStatusCode()+"/"+res.getStatusLine().getReasonPhrase());
                }
            }
        }
    }

    public enum ParameterType {
        BOOLEAN, STRING
    }
    public static class JenkinsParameter {

        private Object defaultValue;
        private String name;
        private ParameterType type;

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ParameterType getType() {
            return type;
        }

        public void setType(ParameterType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "JenkinsParameter{" +
                    "defaultValue=" + defaultValue +
                    ", name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
