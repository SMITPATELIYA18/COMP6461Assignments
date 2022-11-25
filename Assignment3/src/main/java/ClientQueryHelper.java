import java.util.HashMap;

public class ClientQueryHelper {
    private String commandName;
    private String requestMethod;
    private String requestURL;
    private String postData;
    private boolean isHttpHeader;
    private HashMap<String, String> headerValue;

    public ClientQueryHelper() {
        headerValue = new HashMap<>();
    }

    public HashMap<String, String> getHeaderValue() {
        return this.headerValue;
    }

    public void setHeaderValue(HashMap<String, String> headerValue) {
        this.headerValue = headerValue;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    @Override
    public String toString() {
        return "ClientQueryHelper{" +
                "commandName='" + commandName + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestURL='" + requestURL + '\'' +
                ", postData='" + postData + '\'' +
                ", isHttpHeader=" + isHttpHeader +
                '}';
    }

    public boolean isHttpHeader() {
        return isHttpHeader;
    }

    public void setHttpHeader(boolean httpHeader) {
        isHttpHeader = httpHeader;
    }
}
