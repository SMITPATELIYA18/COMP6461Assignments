import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Server {
    public static void main(String[] args) {
        ServerHelper serverHelper = new ServerHelper();
//        String dir = "C:\\Users\\Shree\\Desktop\\COMP6461_A1\\COMP6461Assignments
//        \\Assignment3\\FTPServer\\";
        String dir = "/Users/smitpateliya/COMP6461/COMP6461Assignments/Assignment3/FTPServer/";
        if (args.length == 0) {
            System.out.println("You have not entered port number, Directory and Debug Flag.");
            serverHelper.setPort(8080);
            serverHelper.setDirectory(dir);
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
                serverHelper.setDirectory(dir);
                System.out.println("The directory is " + serverHelper.getDirectory());
            }
        }
        Server server = new Server();
        server.requestServer(serverHelper);
//        ClientQueryHelper clientQueryHelper =server.createQuery("httpfs post -d " +
//                "{Smit:Pateliya} " +
//                "http://localhost:8080/xyz.txt");
//        System.out.println(clientQueryHelper);
//        String result = server.createResponse(clientQueryHelper,
//                serverHelper);
//        System.out.println(result);
//        new Server().requestServer(serverHelper);

    }

    private static String addFilesNameToBody(String tempBody, List<File> directoryFiles) {
        if (directoryFiles.size() == 0) {
            tempBody += "},";
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

    private static void addDataToFile(ClientQueryHelper clientHelper,
                                      File requestedFileObject) throws
            IOException {
        synchronized (requestedFileObject) {
            if (clientHelper.getPostData() != null) {
                FileWriter fileWriter = new FileWriter(requestedFileObject);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter printWriter = new PrintWriter(bufferedWriter);
                printWriter.write(clientHelper.getPostData());
                printWriter.flush();
                printWriter.close();
            }
        }
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

    private void requestServer(ServerHelper serverHelper) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(serverHelper.getPort()));
            ByteBuffer byteBuffer =
                    ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
            if (serverHelper.isDebugFlag())
                System.out.println("ByteBuffer --> " + byteBuffer);
            while (true) {
                byteBuffer.clear();
                SocketAddress routerAddress = channel.receive(byteBuffer);
                if (routerAddress != null) {
                    byteBuffer.flip();
                    Packet routerPacket = Packet.fromBuffer(byteBuffer);
                    byteBuffer.flip();
                    if (serverHelper.isDebugFlag())
                        System.out.println(routerPacket.toString());
                    String routerPayload = new String(routerPacket.getPayload(),
                            StandardCharsets.UTF_8);
                    if (routerPayload.equals("Hi from client")) {
                        if (serverHelper.isDebugFlag())
                            System.out.println("Received: " + routerPayload);
                        Packet serverResponse = routerPacket.toBuilder()
                                .setPayload("Hi from Server".getBytes()).create();
                        channel.send(serverResponse.toBuffer(), routerAddress);
                        if (serverHelper.isDebugFlag())
                            System.out.println("Sending \"Hi from Server\"");
                    } else if (routerPayload.startsWith("httpfs")) {
                        //httpfs get -v -h hello:hello http://localhost:8080
                        //httpfs get -v -h hello:hello http://localhost:8080/xyz.txt
                        //httpfs post -v -h overwrite:true -d {} http://localhost:8080/xyz.txt
                        if (serverHelper.isDebugFlag())
                            System.out.println("Received: " + routerPayload);
                        ClientQueryHelper clientQueryHelper = createQuery(routerPayload);
                        String responseToRouter = createResponse(clientQueryHelper,
                                serverHelper);
                        Packet serverResponse =
                                routerPacket.toBuilder()
                                        .setPayload(responseToRouter.getBytes()).create();
                        channel.send(serverResponse.toBuffer(), routerAddress);
                        if (serverHelper.isDebugFlag())
                            System.out.println("Sending \"" + responseToRouter + "\"");
                    } else if (routerPayload.equals("Received")) {
                        if (serverHelper.isDebugFlag())
                            System.out.println("Received: " + routerPayload);
                        Packet serverResponse =
                                routerPacket.toBuilder().setPayload("Close".getBytes())
                                        .create();
                        channel.send(serverResponse.toBuffer(), routerAddress);
                        if (serverHelper.isDebugFlag())
                            System.out.println("Sending \"Close\"");
                    } else if (routerPayload.equals("Okk")) {
                        System.out.println("Received \"Okk\"");
                    }

                    //Add Else as a 404 Not Found
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private ClientQueryHelper createQuery(String routerPayload) {
        ClientQueryHelper temp = new ClientQueryHelper();
        String[] payloadArray = routerPayload.split(" ");
        for (int i = 0; i < payloadArray.length; i++) {
            if (payloadArray[i].equals("httpfs")) {
                temp.setCommandName("httpfs");
            } else if (payloadArray[i].equals("get") || payloadArray[i].equals("post")) {
                temp.setRequestMethod(payloadArray[i]);
            } else if (payloadArray[i].startsWith("http://")) {
                temp.setRequestURL(payloadArray[i]);
            } else if (payloadArray[i].equals("-h")) {
                HashMap<String, String> headerValue = temp.getHeaderValue();
//                if (!parameters.get(i + 1).contains(":")) {
//                    System.out.println("did not enter one or more correct 'Headers'.");
//                    System.out.println();
//                    return;
//                }
                String[] headerValues = payloadArray[i + 1].split(":");
                headerValue.put(headerValues[0], headerValues[1]);
                temp.setHttpHeader(true);
                temp.setHeaderValue(headerValue);
                i++;
            } else if (payloadArray[i].equals("-d")) {
                temp.setPostData(payloadArray[i + 1]);
                i++;
            }
        }
        return temp;
    }

    private String createResponse(ClientQueryHelper clientQueryHelper,
                                  ServerHelper serverHelper) {
        URI uri;
        try {
            uri = new URI(clientQueryHelper.getRequestURL());
        } catch (NullPointerException | URISyntaxException e) {
//            System.out.println("Hello");
            return createHeaders(ServerHelper.FileNotFoundStatusCode);
        }
        try {
            if (clientQueryHelper.getRequestMethod().equals("get")) {
                if (uri.getPath().equals("")) {
                    return sendFileList(clientQueryHelper, serverHelper, uri);
                } else {
                    return sendFileData(clientQueryHelper, serverHelper, uri);
                }
            } else if (clientQueryHelper.getRequestMethod().equals("post")) {
                return sendPostResponse(clientQueryHelper, serverHelper, uri);
            } else {
//                System.out.println("Hello");
                return createHeaders(ServerHelper.FileNotFoundStatusCode);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return createHeaders(ServerHelper.FileNotFoundStatusCode);
        }
    }

    private String sendFileList(ClientQueryHelper clientQueryHelper,
                                ServerHelper serverHelper, URI uri) throws
            UnknownHostException {
        String headers = "";
        String tempBody = "";
        tempBody += "{\n\t\"args\": {},\n\t\"Headers\": {";
        tempBody += "\n\t\t\"Connection\": \"close\",";
        tempBody += "\n\t\t\"Host\": " + uri.getHost() + "\"\n";
        tempBody += "\t},\n";
        tempBody += "\t\"Files\": {";
        List<File> directoryFiles =
                getFilesFromDirectory(serverHelper.getDirectory());
        if (clientQueryHelper.getHeaderValue().containsKey("Content-Type")) {
            List<File> filteredFiles = new ArrayList<>();
            String requestedFileType = clientQueryHelper.getHeaderValue()
                    .get("Content-Type");
            for (File file : directoryFiles) {
                if (file.getName()
                        .substring(file.getName().lastIndexOf(".") + 1)
                        .equalsIgnoreCase(requestedFileType.substring(requestedFileType.lastIndexOf("/") + 1))) {
                    filteredFiles.add(file);
                }
            }
            if (filteredFiles.size() == 0) {
                headers = createHeaders(ServerHelper.FileNotFoundStatusCode);
            } else {
                headers = createHeaders(ServerHelper.OkStatusCode);
            }
            tempBody = addFilesNameToBody(tempBody, filteredFiles);
        } else {
            if (directoryFiles.size() == 0) {
                headers = createHeaders(ServerHelper.FileNotFoundStatusCode);
            } else {
                headers = createHeaders(ServerHelper.OkStatusCode);
            }
            tempBody = addFilesNameToBody(tempBody, directoryFiles);
        }
        tempBody += "\n\t\"origin\": \"" + InetAddress.getLocalHost()
                .getHostAddress() + "\",\n";
        tempBody += "\t\"url\": \"" + clientQueryHelper.getRequestURL() + "\"\n";
        tempBody += "}";
        return headers + "\n" + tempBody;
    }

    private String sendFileData(ClientQueryHelper clientQueryHelper,
                                ServerHelper serverHelper, URI uri) throws IOException {
        String headers = "";
        String tempBody = "";
        tempBody += "{\n\t\"args\": {},\n\t\"Headers\": {";
        tempBody += "\n\t\t\"Connection\": \"close\",";
        tempBody += "\n\t\t\"Host\": " + uri.getHost() + "\"\n";
        tempBody += "\t},";
        String requestedFileName = uri.getPath().substring(1);
        File requestedFile =
                new File(serverHelper.getDirectory() + requestedFileName);
        String absolutePath = requestedFile.getCanonicalPath().substring(0,
                requestedFile.getCanonicalPath().lastIndexOf("/"));
        String tempDirectory = serverHelper.getDirectory().substring(0,
                serverHelper.getDirectory().length() - 1);
        if (absolutePath.equals(tempDirectory)) {
            List<File> directoryFiles =
                    getFilesFromDirectory(serverHelper.getDirectory());
            boolean checkFileExist = false; // True If Exist
            File requestedFileObject = null;
//                            System.out.println(requestedFile.getName());
            for (File file : directoryFiles) {
//                                System.out.println(file.getName());
                if (file.getName().equals(requestedFile.getName())) {
                    checkFileExist = true;
                    requestedFileObject = file;
                    break;
                }
            }
            if (!checkFileExist) {

                headers = createHeaders(ServerHelper.FileNotFoundStatusCode);
            } else {
                BufferedReader bufferedReader =
                        new BufferedReader(new FileReader(requestedFileObject));
                String tempString, tempData = "";
                while ((tempString = bufferedReader.readLine()) != null) {
                    tempData += tempString;
                }
//                if (clientHelper.getHeaderValue()
//                        .containsKey("Content-Disposition")) {
//                    if (clientHelper.getHeaderValue()
//                            .get("Content-Disposition")
//                            .equalsIgnoreCase(
//                                    "attachment")) {
//                        serverResponse.setCode("203");
//                        serverResponse.setBody(tempData);
//                        serverResponse.setFileName(requestedFileName);
//                    }
//                }
                tempBody += "\n\t\"Data\":\"" + tempData + "\",";
                headers = createHeaders(ServerHelper.OkStatusCode);
            }
        } else {
            headers = createHeaders(ServerHelper.FileNotFoundStatusCode);
        }
        tempBody += "\n\t\"origin\": \"" + InetAddress.getLocalHost()
                .getHostAddress() + "\",\n";
        tempBody += "\t\"url\": \"" + clientQueryHelper.getRequestURL() + "\"\n";
        tempBody += "}";
        return headers + "\n" + tempBody;
    }

    private String sendPostResponse(ClientQueryHelper clientQueryHelper,
                                    ServerHelper serverHelper, URI uri) throws IOException {
        String headers = "";
        String tempBody = "";
        tempBody += "{\n\t\"args\": {},\n\t\"Headers\": {";
        tempBody += "\n\t\t\"Connection\": \"close\",";
        tempBody += "\n\t\t\"Host\": " + uri.getHost() + "\"\n";
        tempBody += "\t},";
        String requestedFileName = uri.getPath().substring(1);
        File requestedFile =
                new File(serverHelper.getDirectory() + requestedFileName);
        String absolutePath = requestedFile.getCanonicalPath().substring(0,
                requestedFile.getCanonicalPath().lastIndexOf("/"));
        String tempDirectory = serverHelper.getDirectory().substring(0,
                serverHelper.getDirectory().length() - 1);
        if (absolutePath.equals(tempDirectory)) {
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
            if (!checkFileExist) {
                requestedFileObject =
                        new File(serverHelper.getDirectory() + "/" + requestedFileName);
                synchronized (requestedFileObject) {
                    requestedFileObject.createNewFile();
                    if (clientQueryHelper.getPostData() != null) {
                        FileWriter fileWriter =
                                new FileWriter(requestedFileObject);
                        BufferedWriter bufferedWriter =
                                new BufferedWriter(fileWriter);
                        PrintWriter printWriter = new PrintWriter(bufferedWriter);
                        printWriter.write(clientQueryHelper.getPostData());
                        printWriter.flush();
                        printWriter.close();
                    }
                }
                headers = createHeaders(ServerHelper.NewFileCreatedStatusCode);
            } else {
                if (clientQueryHelper.getHeaderValue().containsKey("overwrite")) {
                    if (clientQueryHelper.getHeaderValue().get("overwrite")
                            .equalsIgnoreCase(
                                    "true")) {
                        synchronized (requestedFileObject) {
                            if (clientQueryHelper.getPostData() != null) {
                                FileWriter fileWriter =
                                        new FileWriter(requestedFileObject);
                                BufferedWriter bufferedWriter =
                                        new BufferedWriter(fileWriter);
                                PrintWriter printWriter =
                                        new PrintWriter(bufferedWriter);
                                printWriter.write(clientQueryHelper.getPostData());
                                printWriter.flush();
                                printWriter.close();
                            }
                        }
                        headers = createHeaders(ServerHelper.FileOverwrittenStatusCode);
                    } else {
//                                    synchronized (requestedFileObject) {
//
//                                    }
//                                    addDataToFile(serverResponse, clientQueryHelper,
//                                            requestedFileObject);
                        headers = createHeaders(ServerHelper.FileOverwrittenStatusCode);
                    }
                } else {
                    addDataToFile(clientQueryHelper,
                            requestedFileObject);
                    headers = createHeaders(ServerHelper.FileOverwrittenStatusCode);
                }
            }
        } else {
            headers = createHeaders(ServerHelper.FileNotFoundStatusCode);
        }
        tempBody += "\n\t\"origin\": \"" + InetAddress.getLocalHost()
                .getHostAddress() + "\",\n";
        tempBody += "\t\"url\": \"" + clientQueryHelper.getRequestURL() + "\"\n";
        tempBody += "}";
        return headers + "\n" + tempBody;
    }
}
