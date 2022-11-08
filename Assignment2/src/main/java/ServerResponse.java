//-------------------------------------------------
// Assignment 2
// © Smit Pateliya and Raviraj Savaliya
// Written by: Smit Pateliya (40202779) & Raviraj Savaliya (40200503)
//-------------------------------------------------
import java.io.Serializable;

public class ServerResponse implements Serializable {
    private String headers;
    private String body;
    private String code;
    private String message;
    private String fileName;

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
