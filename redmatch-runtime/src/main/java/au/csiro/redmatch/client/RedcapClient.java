/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.client;

import au.csiro.redmatch.importer.RedcapJsonImporter;
import au.csiro.redmatch.model.LabeledDirectedMultigraph;
import au.csiro.redmatch.model.LabeledEdge;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.Schema;
import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * A client to communicate with a REDCap server.
 *
 * @author Alejandro Metke Jimenez
 */
@Component
public class RedcapClient implements Client {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapClient.class);

  private final Gson gson;

  @Autowired
  public RedcapClient(Gson gson) {
    this.gson = gson;
  }

  @Override
  public Schema getSchema(String endpoint, Credentials credentials) {
    return getSchema(endpoint, credentials, Collections.emptySet());
  }

  @Override
  public Schema getSchema(String endpoint, Credentials credentials, Set<String> fieldIds) {
    RedcapCredentials rc = (RedcapCredentials) credentials;

    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("token", rc.getToken()));
    params.add(new BasicNameValuePair("content", "metadata"));
    params.add(new BasicNameValuePair("format", "json"));
    for (String fieldId : fieldIds) {
      params.add(new BasicNameValuePair("fields[]", fieldId));
    }

    final RedcapResponse resp = doPost(endpoint, params);
    String content = resp.getContent();
    handleRedcapStatus(resp.getStatus(), content);

    // The content should now be valid JSON
    RedcapJsonImporter imp = new RedcapJsonImporter(gson);
    return imp.loadSchema(content);
  }

  @Override
  public List<Row> getData(String endpoint, Credentials credentials) {
    return getData(endpoint, credentials, Collections.emptySet());
  }

  @Override
  public List<Row> getData(String endpoint, Credentials credentials, Set<String> fieldIds) {
    RedcapCredentials rc = (RedcapCredentials) credentials;


    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("token", rc.getToken()));
    params.add(new BasicNameValuePair("content", "record"));
    params.add(new BasicNameValuePair("format", "json"));
    for (String fieldId : fieldIds) {
      params.add(new BasicNameValuePair("fields[]", fieldId));
    }

    final RedcapResponse resp = doPost(endpoint, params);
    String content = resp.getContent();
    handleRedcapStatus(resp.getStatus(), content);
    return parseData(content);
  }

  private RedcapResponse doPost(String url, ArrayList<NameValuePair> params) {
    HttpPost post = new HttpPost(url);
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");

    try {
      post.setEntity(new UrlEncodedFormEntity(params));
    } catch (UnsupportedEncodingException e) {
      throw new ClientException("There was a problem related to encoding.", e);
    }

    StringBuilder result = new StringBuilder();
    HttpClient client = HttpClientBuilder.create().build();

    HttpResponse resp;
    try {
      resp = client.execute(post);
    } catch (IOException e) {
      throw new ClientException("There was an I/O issue communicating with REDCap at " + url + ".", e);
    }

    int respCode = resp.getStatusLine().getStatusCode();

    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(resp.getEntity().getContent()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        result.append(line);
        result.append('\n');
      }
    } catch (IOException e) {
      throw new ClientException("There was an I/O issue reading REDCap's response.", e);
    }

    final String content = result.toString();
    checkContentType(resp, content);

    return new RedcapResponse(respCode, content);
  }

  private void checkContentType(HttpResponse resp, String content) {
    final Header[] contentTypeHeaders = resp.getHeaders("Content-Type");
    if (contentTypeHeaders.length >= 1) {
      final String contentType = contentTypeHeaders[0].getValue();
      if (!contentType.startsWith("application/json")) {
        if (log.isErrorEnabled()) {
          log.error("Unexpected REDCap response");
        }
        throw new ClientException("Expected a response from REDCap with content type application/json but was "
          + contentType + ". Content = '" + content + "'.");
      }
    } else {
      log.warn("Content-Type header was not present in REDCap response.");
    }
  }

  private void handleRedcapStatus(int status, String msg) {
    if (status < 200 || status >= 300) {
      throw new ClientException(msg);
    }
  }

  /**
   * The assumption here is that each row contains data about a patient. The basic patient information will exist in the
   * non-repeatable instrument. Additional information can be captured in repeatable or non-repeatable instruments.
   *
   * @param data The JSON string with the data.
   * @return A list of {@link Row}s.
   */
  public List<Row> parseData(String data) {
    final JsonArray rows = gson.fromJson(data, JsonArray.class);
    if (rows.size() == 0) {
      return Collections.emptyList();
    }

    // Find name of key that represents record number
    String uniqueKey = null;
    JsonObject jsonObject = rows.get(0).getAsJsonObject();
    Optional<Map.Entry<String, JsonElement>> first = jsonObject.entrySet().stream().findFirst();
    if (first.isPresent()) {
      uniqueKey = first.get().getKey();
    }
    if (uniqueKey == null) {
      throw new RuntimeException("Could not find unique key. This should not happen!");
    }

    final List<Row> res = new ArrayList<>();

    Map<String, JsonObject> patientObjectMap = new HashMap<>();
    Map<String, List<JsonObject>> repeatableInstrumentsMap = new HashMap<>();
    for (JsonElement row : rows) {
      jsonObject = row.getAsJsonObject();

      String key = jsonObject.get(uniqueKey).getAsString();
      JsonElement repeatableInstrumentElement = jsonObject.get("redcap_repeat_instrument");
      if (repeatableInstrumentElement != null) {
        String repeatableInstrumentName = repeatableInstrumentElement.getAsString();
        if (repeatableInstrumentName == null || repeatableInstrumentName.isEmpty()) {
          // This is a patient instrument
          jsonObject.addProperty(LabeledDirectedMultigraph.VERTEX_TYPE_FIELD, "Patient");
          patientObjectMap.put(key, jsonObject);
        } else {
          // This is a repeatable instrument
          jsonObject.addProperty(LabeledDirectedMultigraph.VERTEX_TYPE_FIELD, "RepeatableInstrument");
          List<JsonObject> patientObjects = repeatableInstrumentsMap.computeIfAbsent(key, k -> new ArrayList<>());
          patientObjects.add(jsonObject);
        }
      } else {
        // Repeatable instruments are not enabled in REDCap
        jsonObject.addProperty(LabeledDirectedMultigraph.VERTEX_TYPE_FIELD, "Patient");
        patientObjectMap.put(key, jsonObject);
      }
    }

    for (String key : patientObjectMap.keySet()) {
      Row entry = new Row();
      Graph<JsonElement, LabeledEdge> graph = new LabeledDirectedMultigraph<>(LabeledEdge.class);
      JsonObject patient = patientObjectMap.get(key);
      graph.addVertex(patient);
      List<JsonObject> repeatableInstruments = repeatableInstrumentsMap.get(key);
      if (repeatableInstruments != null) {
        for (JsonObject repeatableInstrument : repeatableInstruments) {
          graph.addVertex(repeatableInstrument);
          String repeatableInstrumentName = repeatableInstrument.get("redcap_repeat_instrument").getAsString();
          graph.addEdge(patient, repeatableInstrument, new LabeledEdge(repeatableInstrumentName));
        }
      }
      entry.setData(graph);
      res.add(entry);
    }
    return res;
  }

  private static class RedcapResponse {
    private final int status;
    private final String content;

    /**
     * Creates a new response with a status and a content string.
     *
     * @param status The status.
     * @param content The content string.
     */
    public RedcapResponse(int status, String content) {
      this.status = status;
      this.content = content;
    }

    public int getStatus() {
      return status;
    }

    public String getContent() {
      return content;
    }
  }
}
