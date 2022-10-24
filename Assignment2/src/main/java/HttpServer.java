import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerHelper serverHelper = new ServerHelper();
        if (args.length == 0) {
            System.out.println("You have not entered port number, Directory and Debug Flag.");
            serverHelper.setPort(8000);
            serverHelper.setDirectory(System.getProperty("user.dir"));
            serverHelper.setDebugFlag(false);
            System.out.println("The Port Number is " + serverHelper.getPort());
            System.out.println("The directory is " + serverHelper.getDirectory());
            System.out.println("The debug Flag is " + serverHelper.isDebugFlag());
        } else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-v")) {
                    serverHelper.setDebugFlag(true);
                } else if (args[i].equals("-p")) {
                    try {
                        serverHelper.setPort(Integer.parseInt(args[i + 1]));
                    } catch (NumberFormatException e) {
                        System.out.println("You did not enter correct Port Number!!!");
                        System.exit(1);
                    }
                    i++;
                } else if (args[i].equals("-d")) {
                    serverHelper.setDirectory(args[i++].trim());
                }
            }
            if (serverHelper.getPort() == -1) {
                serverHelper.setPort(8000);
                System.out.println("The Port Number is " + serverHelper.getPort());
            }
            if (serverHelper.getDirectory() == null) {
                serverHelper.setDirectory(System.getProperty("user.dir"));
                System.out.println("The directory is " + serverHelper.getDirectory());
            }
        }

        serverHelper.setServerSocket(new ServerSocket(serverHelper.getPort()));
        if (serverHelper.isDebugFlag()) {
            System.out.println("The Server is running on " + serverHelper.getPort() + " port" +
                    " number.");
        }
        try {
            runServer(serverHelper);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println("Hii");
        }
    }

    private static void runServer(ServerHelper serverHelper) throws IOException,
            ClassNotFoundException, URISyntaxException {
        while (true) {
            ServerResponse serverResponse = new ServerResponse();
            Socket socket = serverHelper.getServerSocket().accept();

            ObjectInputStream objectInputStream =
                    new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream =
                    new ObjectOutputStream(socket.getOutputStream());

            HttpcHelper httpcHelper;
            httpcHelper = (HttpcHelper) objectInputStream.readObject();
//            System.out.println(httpcHelper);
            if (httpcHelper.getCommandName().equalsIgnoreCase("httpc")) {
                URI uri = new URI(httpcHelper.getRequestURL());
                if (serverHelper.isDebugFlag()) {
                    System.out.println("Server is Processing: " + httpcHelper.getCommandName() + " " + httpcHelper.getRequestMethod() + " " + httpcHelper.getRequestURL());
                }
                if (httpcHelper.isVerbosePreset()) {
                    serverResponse.setHeaders(createHeaders(ServerHelper.OkStatusCode));
                }

                String tempBody = "";
                if(httpcHelper.getRequestMethod().equalsIgnoreCase("get")) {
                    if(uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                        tempBody +="{\n\t\"args\":{";
                        String[] queryParameters = uri.getQuery().split("&");
                        for(int i=0;i< queryParameters.length;i++) {
                            int equalPosition = queryParameters[i].indexOf("=");
                            tempBody+="\n\t\t\""+queryParameters[i].substring(0,equalPosition)+
                                    "\":\""+queryParameters[i].substring(equalPosition+1)+"\"";
                            if(queryParameters.length-1 != i) {
                                tempBody+=",";
                            } else {
                                tempBody+="\n\t},\n";
                            }
                        }
                    } else {
                        tempBody+="{\n\t\"args\":{},\n";
                    }
                } else if(httpcHelper.getRequestMethod().equalsIgnoreCase("post")) {
                    tempBody+="{\n\t\"args\": {},";
                    if(httpcHelper.isInlineData() || httpcHelper.isFileSend())
                        tempBody+="\n\t\"data\":\""+httpcHelper.getPostData()+"\",\n";
                    else
                        tempBody+= "\n\t\"data\": {},\n";
                    tempBody+= "\t\"files\": {},\n";
                    tempBody+= "\t\"form\": {},\n";
                }

                tempBody+="\t\"headers\": {";
                if(httpcHelper.isHttpHeader()) {
                    for(Map.Entry<String,String> entry :
                            httpcHelper.getHeaderValue().entrySet()) {
                        if(entry.getKey().equalsIgnoreCase("connection"))
                            continue;
                        tempBody+="\n\t\t\""+entry.getKey()+"\":\""+entry.getValue()+"\",";
                    }
                }
                if(httpcHelper.isInlineData() || httpcHelper.isFileSend())
                    tempBody+="\n\t\t\"Content-Length\": \""+ httpcHelper.getPostData().length()+"\",";
                tempBody+="\n\t\tHost: \""+uri.getHost()+"\"\n\t},\n";
                //Headers Over

                if(httpcHelper.getRequestMethod().equalsIgnoreCase("post")) {
                    tempBody+="\t\"json\": null,\n";
                }
                tempBody+="\t\"origin\": \""+ InetAddress.getLocalHost().getHostAddress()+
                        "\",\n";
                tempBody+="\t\"url\": \"" + httpcHelper.getRequestURL() + "\"\n}";
                //Body Finishes

                serverResponse.setBody(tempBody);
                objectOutputStream.writeObject(serverResponse);
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
                if(serverHelper.isDebugFlag())
                    System.out.println("Response has been sent!!");
            }
        }
    }

    private static String createHeaders(String statusCode) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date today = new Date();
        String timeStamp = dateFormat.format(today);
        return statusCode + "\n" + ServerHelper.ConnectionAlive + "\n" + ServerHelper.Server + "\n" + ServerHelper.Date + timeStamp
                + "\n" + ServerHelper.AccessControlAllowOrigin + "\n" + ServerHelper.AccessControlAllowCredentials + "\n" + ServerHelper.Via + "\n";
    }
}
