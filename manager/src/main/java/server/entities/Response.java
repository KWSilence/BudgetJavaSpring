package server.entities;

import com.google.gson.annotations.Expose;

public class Response
{
  @Expose
  Object body;
  @Expose
  Boolean success;
  @Expose
  String error;

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
}
