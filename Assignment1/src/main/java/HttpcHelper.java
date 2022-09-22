public class HttpcHelper {
    private String requestURL;
    private String inlineData;
    private String redirectLocation;
    private String httpRequest;
    private String requestMethod;
    private String fileSendPath;
    private String fileWritePath;
    private boolean isVerbosePreset;
    private boolean isHttpHeader;
    private boolean isInlineData;
    private boolean isFileSend;
    private boolean isFileWrite;
    private boolean isRedirect;

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getInlineData() {
        return inlineData;
    }

    public String getRedirectLocation() {
        return redirectLocation;
    }

    public void setRedirectLocation(String redirectLocation) {
        this.redirectLocation = redirectLocation;
    }

    public String getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(String httpRequest) {
        this.httpRequest = httpRequest;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
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

    public void setInlineData(String inlineData) {
        this.inlineData = inlineData;
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

    public boolean isRedirect() {
        return isRedirect;
    }

    public void setRedirect(boolean redirect) {
        isRedirect = redirect;
    }


}