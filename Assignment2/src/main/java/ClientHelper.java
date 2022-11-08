//-------------------------------------------------
// Assignment 2
// © Smit Pateliya and Raviraj Savaliya
// Written by: Smit Pateliya (40202779) & Raviraj Savaliya (40200503)
//-------------------------------------------------

import java.io.Serializable;
import java.util.HashMap;

/**
 * This is a helper class for HttpcLibrary class.
 */
public class ClientHelper implements Serializable {
    private String commandName;
    private String requestMethod;
    private String requestURL;
    private String postData;
    private String fileSendPath;
    private String fileWritePath;
    private boolean isVerbosePreset;
    private boolean isHttpHeader;
    private boolean isInlineData;
    private boolean isFileSend;
    private boolean isFileWrite;

    private HashMap<String, String> headerValue = new HashMap<>();

    @Override
    public String toString() {
        return "HttpcHelper{" +
                "commandName='" + commandName + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestURL='" + requestURL + '\'' +
                ", postData='" + postData + '\'' +
                ", isVerbosePreset=" + isVerbosePreset +
                '}';
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

    public String getFileSendPath() {
        return fileSendPath;
    }

    public void setFileSendPath(String fileSendPath) {
        this.fileSendPath = fileSendPath;
    }

    public String getFileWritePath() {
        return fileWritePath;
    }

    public void setFileWritePath(String fileWritePath) {
        this.fileWritePath = fileWritePath;
    }

    public boolean isVerbosePreset() {
        return isVerbosePreset;
    }

    public void setVerbosePreset(boolean verbosePreset) {
        isVerbosePreset = verbosePreset;
    }

    public boolean isHttpHeader() {
        return isHttpHeader;
    }

    public void setHttpHeader(boolean httpHeader) {
        isHttpHeader = httpHeader;
    }

    public boolean isInlineData() {
        return isInlineData;
    }

    public void setInlineData(boolean inlineData) {
        isInlineData = inlineData;
    }

    public boolean isFileSend() {
        return isFileSend;
    }

    public void setFileSend(boolean fileSend) {
        isFileSend = fileSend;
    }

    public boolean isFileWrite() {
        return isFileWrite;
    }

    public void setFileWrite(boolean fileWrite) {
        isFileWrite = fileWrite;
    }

}