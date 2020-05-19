package server.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

public class Response
{
  @Expose
  private Object body;
  @Expose
  private Boolean success;
  @Expose
  private String error;

  public Response(Object body, Boolean success, String error)
  {
    this.body = body;
    this.success = success;
    this.error = error;
  }

  public Response()
  {
    this.error = "";
    this.body = "";
    this.success = true;
  }

  public Response(Object body)
  {
    this.error = "";
    this.success = true;
    this.body = body;
  }

  public Response(String error, Boolean success)
  {
    this.error = error;
    this.success = success;
    this.body = "";
  }

  public Object getBody()
  {
    return body;
  }

  public ArrayList<LinkedTreeMap<String, Object>> getArrayBody()
  {
    return (ArrayList<LinkedTreeMap<String, Object>>) body;
  }

  public LinkedTreeMap<String, Object> getMapBody()
  {
    return (LinkedTreeMap<String, Object>) body;
  }

  public static Response parseJson(String json)
  {
    Gson gson = new Gson();
    return gson.fromJson(json, Response.class);
  }

  public Boolean getSuccess()
  {
    return success;
  }

  public String getError()
  {
    return error;
  }
}
