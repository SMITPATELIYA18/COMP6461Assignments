import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerHelper serverHelper = new ServerHelper();
        if (args.length == 0) {
            System.out.println("You have not entered port number, Directory and Debug Flag.");
            serverHelper.setPort(8000);
            serverHelper.setDirectory("/Users/smitpateliya/COMP6461/COMP6461Assignments" +
                    "/Assignment2/FTPServer/");
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
                serverHelper.setDirectory("/Users/smitpateliya/COMP6461/COMP6461Assignments" +
                        "/Assignment2/FTPServer/");
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
//            System.out.println("Hii");
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

            ClientHelper clientHelper;
            clientHelper = (ClientHelper) objectInputStream.readObject();
//            System.out.println(clientHelper);
            System.out.println(clientHelper);
            if (clientHelper.getCommandName().equalsIgnoreCase("httpc")) {
                URI uri = new URI(clientHelper.getRequestURL());
                if (serverHelper.isDebugFlag()) {
                    System.out.println("Server is Processing: " + clientHelper.getCommandName() + " " + clientHelper.getRequestMethod() + " " + clientHelper.getRequestURL());
                }
//                if (clientHelper.isVerbosePreset()) {
//                }
                serverResponse.setHeaders(createHeaders(ServerHelper.OkStatusCode));

                String tempBody = "";
                if (clientHelper.getRequestMethod().equalsIgnoreCase("get")) {
                    if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                        tempBody += "{\n\t\"args\":{";
                        String[] queryParameters = uri.getQuery().split("&");
                        for (int i = 0; i < queryParameters.length; i++) {
                            int equalPosition = queryParameters[i].indexOf("=");
                            tempBody += "\n\t\t\"" + queryParameters[i].substring(0,
                                    equalPosition) +
                                    "\":\"" + queryParameters[i].substring(equalPosition + 1) + "\"";
                            if (queryParameters.length - 1 != i) {
                                tempBody += ",";
                            } else {
                                tempBody += "\n\t},\n";
                            }
                        }
                    } else {
                        tempBody += "{\n\t\"args\":{},\n";
                    }
                } else if (clientHelper.getRequestMethod().equalsIgnoreCase("post")) {
                    tempBody += "{\n\t\"args\": {},";
                    if (clientHelper.isInlineData() || clientHelper.isFileSend())
                        tempBody += "\n\t\"data\":\"" + clientHelper.getPostData() + "\",\n";
                    else
                        tempBody += "\n\t\"data\": {},\n";
                    tempBody += "\t\"files\": {},\n";
                    tempBody += "\t\"form\": {},\n";
                }

                tempBody += "\t\"headers\": {";
                if (clientHelper.isHttpHeader()) {
                    for (Map.Entry<String, String> entry :
                            clientHelper.getHeaderValue().entrySet()) {
                        if (entry.getKey().equalsIgnoreCase("connection"))
                            continue;
                        tempBody += "\n\t\t\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",";
                    }
                }
                if (clientHelper.isInlineData() || clientHelper.isFileSend())
                    tempBody += "\n\t\t\"Content-Length\": \"" + clientHelper.getPostData()
                            .length() + "\",";
                tempBody += "\n\t\t\"Host\": \"" + uri.getHost() + "\"\n\t},\n";
                //Headers Over

                if (clientHelper.getRequestMethod().equalsIgnoreCase("post")) {
                    tempBody += "\t\"json\": null,\n";
                }
                tempBody += "\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() +
                        "\",\n";
                tempBody += "\t\"url\": \"" + clientHelper.getRequestURL() + "\"\n}";
                //Body Finishes
                serverResponse.setCode("200");
                serverResponse.setBody(tempBody);
                objectOutputStream.writeObject(serverResponse);
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
                if (serverHelper.isDebugFlag())
                    System.out.println("Response has been sent!!");
            } else if (clientHelper.getCommandName().equalsIgnoreCase("httpfs")) {
                URI uri = new URI(clientHelper.getRequestURL());
                if (serverHelper.isDebugFlag()) {
                    System.out.println("Server is Processing: " + clientHelper.getCommandName() + " " + clientHelper.getRequestMethod() + " " + clientHelper.getRequestURL());
                }
                String tempBody = "";

                tempBody += "{\n\t\"args\": {},\n\t\"Headers\": {";
                if (clientHelper.getRequestMethod()
                        .equalsIgnoreCase("\\get") && clientHelper.getHeaderValue()
                        .containsKey("Content-Disposition")) {
                    tempBody += "\n\t\t\"Content-Disposition\": \"" + clientHelper.getHeaderValue()
                            .get("Content-Disposition") + "\",";
                }
                tempBody += "\n\t\t\"Connection\": \"close\",";
                tempBody += "\n\t\t\"Host\": " + uri.getHost() + "\"\n";
                tempBody += "\t},\n";
                if (clientHelper.getRequestMethod().equalsIgnoreCase("get")) {
                    if (uri.getPath().equals("")) {
                        tempBody += "\t\"Files\": {";
                        List<File> directoryFiles =
                                getFilesFromDirectory(serverHelper.getDirectory());
                        if (clientHelper.getHeaderValue().containsKey("Content-Type")) {
                            List<File> filteredFiles = new ArrayList<>();
                            String requestedFileType = clientHelper.getHeaderValue()
                                    .get("Content-Type");
                            for (File file : directoryFiles) {
//                                System.out.println(file.getName().substring(file.getName().lastIndexOf(".")));
//                                System.out.println(requestedFileType.substring(requestedFileType.lastIndexOf("/")));
                                if (file.getName().substring(file.getName().lastIndexOf(".")+1)
                                        .equalsIgnoreCase(requestedFileType.substring(requestedFileType.lastIndexOf("/")+1))) {
                                    filteredFiles.add(file);
                                }
                            }
                            tempBody = addFilesNameToBody(tempBody, filteredFiles);
                        } else {
                            tempBody = addFilesNameToBody(tempBody, directoryFiles);
                        }
                        serverResponse.setCode("200");
                        serverResponse.setHeaders(createHeaders(ServerHelper.OkStatusCode));
                    } else {
                        String requestedFileName = uri.getPath().substring(1);
                        List<File> directoryFiles =
                                getFilesFromDirectory(serverHelper.getDirectory());
                        boolean checkFileExist = false; // True If Exist
                        File requestedFileObject = null;
                        for (File file : directoryFiles) {
                            System.out.println(file.getName());
                            System.out.println(requestedFileName);
                            if (file.getName().equals(requestedFileName)) {
                                checkFileExist = true;
                                requestedFileObject = file;
                                break;
                            }
                        }
                        if (!checkFileExist) {
                            serverResponse.setCode("404");
                            serverResponse.setHeaders(createHeaders(ServerHelper.FileNotFoundStatusCode));
                        } else {
                            BufferedReader bufferedReader =
                                    new BufferedReader(new FileReader(requestedFileObject));
                            String tempString, tempData = "";
                            while ((tempString = bufferedReader.readLine()) != null) {
                                tempData += tempString;
                            }
                            if (clientHelper.getHeaderValue()
                                    .containsKey("Content-Disposition")) {
                                if (clientHelper.getHeaderValue().get("Content-Disposition")
                                        .equalsIgnoreCase(
                                                "attachment")) {
                                    serverResponse.setCode("203");
                                    serverResponse.setBody(tempData);
                                    serverResponse.setFileName(requestedFileName);
                                }
                            }
                            serverResponse.setCode("203");
                            tempBody += "\t\"Data\":\"" + tempData + "\",";
                            serverResponse.setHeaders(createHeaders(ServerHelper.OkStatusCode));
                        }
                    }
                } else if (clientHelper.getRequestMethod().startsWith("post")) {
                    String requestedFileName = uri.getPath().substring(1);
                    List<File> directoryFiles =
                            getFilesFromDirectory(serverHelper.getDirectory());
                    boolean checkFileExist = false; // True If Exist
                    File requestedFileObject = null;
                    for (File file : directoryFiles) {
                        if (file.getName().equals(requestedFileName)) {
                            checkFileExist = true;
                            requestedFileObject = file;
                            break;
                        }
                    }
                    System.out.println(checkFileExist);
                    if (!checkFileExist) {
                        requestedFileObject =
                                new File(serverHelper.getDirectory()+"/"+requestedFileName);
                        synchronized (requestedFileObject) {
                            requestedFileObject.createNewFile();
                            if(clientHelper.getPostData()!= null) {
                                FileWriter fileWriter = new FileWriter(requestedFileObject);
                                BufferedWriter  bufferedWriter = new BufferedWriter(fileWriter);
                                PrintWriter printWriter = new PrintWriter(bufferedWriter);
                                printWriter.write(clientHelper.getPostData());
                                printWriter.flush();
                                printWriter.close();
                            }
                        }
                        serverResponse.setCode("202");
                        serverResponse.setHeaders(createHeaders(ServerHelper.NewFileCreatedStatusCode));
                    } else {
                        if(clientHelper.getHeaderValue().containsKey("append")) {
                            if(clientHelper.getHeaderValue().get("append").equalsIgnoreCase(
                                    "true")) {
                                synchronized (requestedFileObject) {
                                    if(clientHelper.getPostData()!= null) {
                                        FileWriter fileWriter =
                                                new FileWriter(requestedFileObject,true);
                                        BufferedWriter  bufferedWriter = new BufferedWriter(fileWriter);
                                        PrintWriter printWriter = new PrintWriter(bufferedWriter);
                                        printWriter.write(clientHelper.getPostData());
                                        printWriter.flush();
                                        printWriter.close();
                                    }
                                }
                                serverResponse.setCode("201");
                                serverResponse.setHeaders(createHeaders(ServerHelper.FileOverwrittenStatusCode));
                            } else {
                                addDataToFile(serverResponse, clientHelper,
                                        requestedFileObject);
                            }
                        } else {
                            addDataToFile(serverResponse, clientHelper, requestedFileObject);
                        }
                    }
                }
                tempBody += "\n\t\"origin\": \"" + InetAddress.getLocalHost()
                        .getHostAddress() + "\",\n";
                tempBody += "\t\"url\": \"" + clientHelper.getRequestURL() + "\"\n";
                tempBody += "}";

                serverResponse.setBody(tempBody);
                objectOutputStream.writeObject(serverResponse);
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
                if (serverHelper.isDebugFlag())
                    System.out.println("Response has been sent!!");
            }
        }
    }

    private static String addFilesNameToBody(String tempBody, List<File> directoryFiles) {
        if(directoryFiles.size() == 0) {
            tempBody+="},";
        }
        for (int i = 0; i < directoryFiles.size(); i++) {
            if (i != directoryFiles.size() - 1) {
                tempBody += "\n\t\t" + directoryFiles.get(i)
                        .getName() + " " + directoryFiles.get(i)
                        .getTotalSpace() + ",";
            } else {
                tempBody += "\n\t\t" + directoryFiles.get(i)
                        .getName() + " " + directoryFiles.get(i)
                        .getTotalSpace() + "\n\t},";
            }
        }
        return tempBody;
    }

    private static void addDataToFile(ServerResponse serverResponse, ClientHelper clientHelper, File requestedFileObject) throws
            IOException {
        synchronized (requestedFileObject) {
            if(clientHelper.getPostData()!= null) {
                FileWriter fileWriter = new FileWriter(requestedFileObject);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter printWriter = new PrintWriter(bufferedWriter);
                printWriter.write(clientHelper.getPostData());
                printWriter.flush();
                printWriter.close();
            }
        }
        serverResponse.setCode("203");
        serverResponse.setHeaders(createHeaders(ServerHelper.FileOverwrittenStatusCode));
    }

    private static List<File> getFilesFromDirectory(String directory) {
        File tempFile = new File(directory);
        List<File> filesList = new ArrayList<>();
        for (File file : tempFile.listFiles()) {
            if (!file.isDirectory())
                filesList.add(file);
        }
        return filesList;
    }

    private static String createHeaders(String statusCode) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date today = new Date();
        String timeStamp = dateFormat.format(today);
        return statusCode + "\n" + ServerHelper.ConnectionAlive + "\n" + ServerHelper.Server + "\n" + ServerHelper.Date + timeStamp
                + "\n" + ServerHelper.AccessControlAllowOrigin + "\n" + ServerHelper.AccessControlAllowCredentials + "\n" + ServerHelper.Via + "\n";
    }
}
