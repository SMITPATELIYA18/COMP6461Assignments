//-------------------------------------------------
// Assignment 2
// © Smit Pateliya and Raviraj Savaliya
// Written by: Smit Pateliya (40202779) & Raviraj Savaliya (40200503)
//-------------------------------------------------
import java.net.ServerSocket;

public class ServerHelper {

    static final String Server = "Server: httpfs/1.0.0";
    static final String Date = "Date: ";
    static final String AccessControlAllowOrigin = "Access" +
            "-Control-Allow-Origin: *";
    static final String AccessControlAllowCredentials =
            "Access-Control-Allow" + "-Credentials: true";
    static final String Via = "Via : 1.1 vegur";
    static final String OkStatusCode = "HTTP/1.1 200 OK";
    static final String FileNotFoundStatusCode = "HTTP/1.1 404 " +
            "FILE NOT FOUND";
    static final String FileOverwrittenStatusCode = "HTTP/1.1 " +
            "201 FILE OVER-WRITTEN";
    static final String FileNotOverwrittenStatusCode =
            "HTTP/1.1 201 FILE NOT OVER-WRITTEN";
    static final String NewFileCreatedStatusCode = "HTTP/1.1 " +
            "202 NEW FILE CREATED";
    static final String ConnectionAlive = "Connection: keep-alive";

    private int port = -1;
    private String directory = null;
    private boolean debugFlag = false;
    private ServerSocket serverSocket = null;

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public boolean isDebugFlag() {
        return debugFlag;
    }

    public void setDebugFlag(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }
}
