import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class FtpcLibrary {
    private final String NewLineCharacter = "\r\n";

    private static void printOnConsole(ClientHelper httpcHelper,
                                       ServerResponse serverResponse) throws
            IOException {
        //System.out.println("Server responce :" + serverResponse.getCode());
        if(serverResponse.getCode().equals("404")) {
        	System.out.println("The server has not found anything matching the Request-URI.");
            System.out.println(serverResponse.getHeaders());
            return;
        }
        if(serverResponse.getCode().equals("200")) {
        	System.out.println("The request has succeed.");
        	System.out.println(serverResponse.getHeaders());
        	return;
        }
        if(serverResponse.getCode().equals("201")) {
        	System.out.println("The request has succeeded and data is overwrite in requested file.");
        	System.out.println(serverResponse.getHeaders());
        	return;
        }
        if(serverResponse.getCode().equals("202")) {
        	System.out.println("The request has been fulfilled and resulted in a new resource being created.");
        	System.out.println(serverResponse.getHeaders());
        	return;
        	
        }
        if(serverResponse.getCode().equals("203")) {
        	System.out.println("The request has succeed.");
        	System.out.println(serverResponse.getHeaders());
        	return;
        }
        
        if (httpcHelper.isVerbosePreset()) {
            System.out.println(serverResponse.getHeaders());
            System.out.println(serverResponse.getBody());
        } else {
            System.out.println(serverResponse.getBody());
        }
    }

    private static void printInFile(ClientHelper getHelper, ServerResponse serverResponse) throws
            IOException {
        FileWriter fileWriter = new FileWriter(getHelper.getFileWritePath(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        String line;

        printWriter.println("Timestamp: " + new Timestamp(System.currentTimeMillis()));
        printWriter.println();

        if (getHelper.isVerbosePreset()) {
            //printWriter.println(status);
            printWriter.println(serverResponse.getHeaders());
        }
//	        else {
//	            while ((line = bufferedReader.readLine()) != null) {
//	                if (line.equals(""))
//	                    break;
//	            }
//	        }
//	        while ((line = bufferedReader.readLine()) != null) {
//	            printWriter.println(line);
//	        }
        printWriter.println(serverResponse.getBody());
        printWriter.println();
        System.out.println("Response has been written in " + getHelper.getFileWritePath() +
                ".");
        printWriter.flush();
        printWriter.close();

    }

    public void get(List<String> parameters) throws IOException, ClassNotFoundException {
        ClientHelper clientHelper = new ClientHelper();
        clientHelper.setCommandName(Ftpc.HTTPFS);
        clientHelper.setRequestMethod("get");
//        System.out.println(parameters);
//        System.out.println(parameters.size());
        for (int i = 2; i < parameters.size(); i++) {
//            System.out.println(parameters.get(i) + i);
            if (parameters.get(i).equals("-v")) {
                clientHelper.setVerbosePreset(true);
            } else if (parameters.get(i).equals("-h")) {
                HashMap<String, String> headerValue = clientHelper.getHeaderValue();
                if (!parameters.get(i + 1).contains(":")) {
                    System.out.println("did not enter one or more correct 'Headers'.");
                    System.out.println();
                    return;
                }
                String[] headerValues = parameters.get(i + 1).split(":");
                headerValue.put(headerValues[0], headerValues[1]);
                clientHelper.setHttpHeader(true);
                clientHelper.setHeaderValue(headerValue);
                i++;
            } else if (parameters.get(i).startsWith("http://") || parameters.get(i)
                    .startsWith("https://")) {
                clientHelper.setRequestURL(parameters.get(i));
             //   System.out.println(clientHelper.getRequestURL());
            } else if (parameters.get(i).equals("-o")) {
                clientHelper.setFileWrite(true);
                clientHelper.setFileWritePath(parameters.get(i + 1));
                i++;
            } else {
                System.out.println("Could Not Find an Option or FLag \"" + parameters.get(i) +
                        "\"");
                System.out.println();
                return;
            }
        }
        URI uri;

        try {
            uri = new URI(clientHelper.getRequestURL());
        } catch (NullPointerException | URISyntaxException e) {
            System.out.println("Please, Enter correct URL!!");
            if (clientHelper.getRequestURL() == null) {
                System.out.println("URL is empty!!");
            } else {
                System.out.println("You have entered " + clientHelper.getRequestURL());
            }
            System.out.println("PLease, Try again!!!");
            return;
        }

        String host = uri.getHost();
        Socket socket;
        try {
            socket = new Socket(host, uri.getPort());
        } catch (UnknownHostException e) {
            System.out.println("Host is not found. " + host);
            return;
        } catch (ConnectException e) {
            System.out.println("Connection Refused. Please check URL!!");
            return;
        }

        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(clientHelper);
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        ServerResponse serverResponse = (ServerResponse) inputStream.readObject();
       // System.out.println(serverResponse);
        if (clientHelper.isFileWrite()) {
            printInFile(clientHelper, serverResponse);
        } else {
            printOnConsole(clientHelper, serverResponse);
        }
    }

    public void post(List<String> parameters) throws IOException, ClassNotFoundException {
        StringBuilder fileData = null;
        ClientHelper postHelper = new ClientHelper();
        postHelper.setCommandName(Ftpc.HTTPFS);
        postHelper.setRequestMethod("post");
        if (parameters.contains("-d") && parameters.contains("-f")) {
            System.out.println("Have entered '-d' and '-f in a command.'");
            System.out.println();
            System.out.println("Run 'httpc help or httpc help <Command>' for available " +
                    "httpc commands and options.");
            return;
        }
        for (int i = 2; i < parameters.size(); i++) {
            if (parameters.get(i).equals("-v")) {
                postHelper.setVerbosePreset(true);
            } else if (parameters.get(i).equals("-h")) {
                HashMap<String, String> headerValue = postHelper.getHeaderValue();
                if (!parameters.get(i + 1).contains(":")) {
                    System.out.println("did not enter one or more correct 'Headers'.");
                    System.out.println();
                    return;
                }
                String[] headerValues = parameters.get(i + 1).split(":");
                headerValue.put(headerValues[0], headerValues[1]);
                postHelper.setHttpHeader(true);
                postHelper.setHeaderValue(headerValue);
                i++;
            } else if (parameters.get(i).startsWith("http://") || parameters.get(i)
                    .startsWith("https://")) {
                postHelper.setRequestURL(parameters.get(i));
            } else if (parameters.get(i).equals("-o")) {
                postHelper.setFileWrite(true);
                postHelper.setFileWritePath(parameters.get(i + 1));
                i++;
            } else if (parameters.get(i).equals("-d")) {
                postHelper.setInlineData(true);
                postHelper.setPostData(parameters.get(i + 1));
                i++;
            } else if (parameters.get(i).equals("-f")) {
                postHelper.setFileSend(true);
                postHelper.setFileSendPath(parameters.get(i + 1));
                fileData = new StringBuilder();
                i++;
            } else {
                System.out.println("Could Not Find an Option or FLag \"" + parameters.get(i) +
                        "\"");
                System.out.println();
                return;
            }
        }
        URI uri;
        try {
            uri = new URI(postHelper.getRequestURL());
        } catch (NullPointerException | URISyntaxException e) {
            System.out.println("Please, Enter correct URL!!");
            if (postHelper.getRequestURL() == null) {
                System.out.println("URL is empty!!");
            } else {
                System.out.println("You have entered " + postHelper.getRequestURL());
            }
            System.out.println("PLease, Try again!!!");
            return;
        }
        String host = uri.getHost();
        Socket socket;
        try {
            socket = new Socket(host, uri.getPort());
        } catch (UnknownHostException e) {
            System.out.println("Host is not found. " + host);
            return;
        } catch (ConnectException e) {
            System.out.println("Connection Refused. Please check URL!!");
            return;
        }

        if (postHelper.isInlineData()) {
            if (postHelper.getPostData().contains("'")) {
                postHelper.setPostData(postHelper.getPostData().replace("'", ""));
            }
            if (postHelper.getPostData().equals("\"")) {
                postHelper.setPostData(postHelper.getPostData().replace("'\"", ""));
            }
        } else if (postHelper.isFileSend()) {
            try{
                File dataFile = new File(postHelper.getFileSendPath());
                BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
                String tempFileData;
                while ((tempFileData = fileReader.readLine()) != null) {
                    fileData.append(tempFileData);
                }
                postHelper.setPostData(fileData.toString());
                fileReader.close();
            }
            catch (Exception e){
                System.out.println("Enter Correct File");
                return;
            }

        }

        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(postHelper);
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        ServerResponse serverResponse = (ServerResponse) inputStream.readObject();


        if (postHelper.isFileWrite()) {
            printInFile(postHelper, serverResponse);
        } else {
            printOnConsole(postHelper, serverResponse);
        }
    }


}
