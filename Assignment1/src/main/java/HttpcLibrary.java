//-------------------------------------------------
// Assignment 1
// © Smit Pateliya and Raviraj Savaliya
// Written by: Smit Pateliya (40202779) & Raviraj Savaliya (40200503)
//-------------------------------------------------

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is Httpc library class.
 * Help, Get and Post method executes in this class and return results.
 */
public class HttpcLibrary {

    /**
     * New Line Character
     */
    private final String NewLineCharacter = "\r\n";

    /**
     * Empty Constructor
     */
    public HttpcLibrary() {
    }

    /**
     * This method prints result on the Console.
     * @param bufferedReader Result from socket class.
     * @param status Status of the command
     * @param getHelper Helper object from Get or Post method
     * @throws IOException throws if there is an error in file reading or writing
     */
    private static void printOnConsole(BufferedReader bufferedReader, String status,
                                       HttpcHelper getHelper) throws IOException {
        String line;
        if (getHelper.isVerbosePreset()) {
            System.out.println(status);
        } else {
            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(line);
                if (line.equals(""))
                    break;
            }
        }
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
    }

    /**
     * This method prints result in the given file.
     * @param bufferedReader Result from socket class.
     * @param status Status of the command
     * @param getHelper Helper object from Get or Post method
     * @throws IOException throws if there is an error in file reading or writing
     */
    private static void printInFile(BufferedReader bufferedReader, String status,
                                    HttpcHelper getHelper) throws IOException {
        FileWriter fileWriter = new FileWriter(getHelper.getFileWritePath(), true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        String line;

        printWriter.println("Timestamp: " + new Timestamp(System.currentTimeMillis()));
        printWriter.println();

        if (getHelper.isVerbosePreset()) {
            printWriter.println(status);
        } else {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals(""))
                    break;
            }
        }
        while ((line = bufferedReader.readLine()) != null) {
            printWriter.println(line);
        }
        printWriter.println();
        System.out.println("Response has been written in " + getHelper.getFileWritePath() +
                ".");
        printWriter.flush();
        printWriter.close();

    }

    /**
     * This method executes help command.
     * @param parameters Httpc command parameters
     */
    public void help(List<String> parameters) {
        if (parameters.size() == 2) {
            System.out.println("httpc is a curl-like application but supports HTTP protocol " +
                    "only.\n" +
                    "Usage: \n\thttpc command [arguments] \nThe commands are: \n\tget \t" +
                    "executes a " +
                    "HTTP GET request and prints the response.\n\tpost \texecutes a HTTP " +
                    "POST" +
                    "request and prints the response. \n\thelp \tprints this screen. \n\nUse" +
                    "\"httpc " +
                    "help [command]\" for more information about a command.");
            return;
        }
        if (parameters.get(2).equals("get")) {
            System.out.println("usage: httpc get [-v] [-h key:value] URL \nGet executes a " +
                    "HTTP GET request for a given URL. \n\t-v Prints the detail of the " +
                    "response such as protocol, status, and headers. \n\t-h key:value " +
                    "Associates headers to HTTP Request with the format 'key:value'.\n\t-o " +
                    "filename.txt Print result in the file");
            return;
        }
        if (parameters.get(2).equals("post")) {
            System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f " +
                    "file]" +
                    " URL \nPost executes a HTTP POST request for a given URL with inline " +
                    "data " +
                    "or from file. \n\t-v Prints the detail of the response such as " +
                    "protocol, " +
                    "status, and headers. \n\t-h key:value Associates headers to HTTP " +
                    "Request " +
                    "with the format 'key:value'. \n\t-d string Associates an inline data to" +
                    " the" +
                    " body HTTP POST request. \n\t-f file Associates the content of a file " +
                    "to " +
                    "the body HTTP POST request. \n\t-o filename.txt Print result in the " +
                    "file \nEither [-d] or [-f] can be used but not " +
                    "both.");
            return;
        }
        System.out.println("Could Not Find an Option or FLag \"" + parameters.get(2) +
                "\"");
        System.out.println();
        System.out.println("Run 'httpc help or httpc help <Command>' for available " +
                "httpc commands and options.");
    }

    /**
     * This method executes get Command
     * @param parameters Httpc command parameters
     * @throws IOException throws if there is an error in file reading or writing
     */
    public void get(List<String> parameters) throws IOException {
        HttpcHelper getHelper = new HttpcHelper();
        for (int i = 2; i < parameters.size(); i++) {
            if (parameters.get(i).equals("-v")) {
                getHelper.setVerbosePreset(true);
            }
            else if (parameters.get(i).equals("-h")) {
                HashMap<String, String> headerValue = getHelper.getHeaderValue();
                if (!parameters.get(i + 1).contains(":")) {
                    System.out.println("did not enter one or more correct 'Headers'.");
                    System.out.println();
                    System.out.println("Run 'httpc help or httpc help <Command>' for " +
                            "available " +
                            "httpc commands and options.");
                    return;
                }
                String[] headerValues = parameters.get(i + 1).split(":");
                headerValue.put(headerValues[0], headerValues[1]);
                getHelper.setHttpHeader(true);
                getHelper.setHeaderValue(headerValue);
                i++;
            } else if (parameters.get(i).startsWith("http://") || parameters.get(i)
                    .startsWith("https://")) {
                getHelper.setRequestURL(parameters.get(i));
            } else if (parameters.get(i).equals("-o")) {
                getHelper.setFileWrite(true);
                getHelper.setFileWritePath(parameters.get(i + 1));
                i++;
            } else {
                System.out.println("Could Not Find an Option or FLag \"" + parameters.get(i) +
                        "\"");
                System.out.println();
                System.out.println("Run 'httpc help or httpc help <Command>' for available " +
                        "httpc commands and options.");
                return;
            }
        }
        URI uri;
        try {
            uri = new URI(getHelper.getRequestURL());
        } catch (NullPointerException | URISyntaxException e) {
            System.out.println("Please, Enter correct URL!!");
            if (getHelper.getRequestURL() == null) {
                System.out.println("URL is empty!!");
            } else {
                System.out.println("You have entered " + getHelper.getRequestURL());
            }
            System.out.println("PLease, Try again!!!");
            return;
        }
        String host = uri.getHost();
        Socket socket;
        try {
            socket = new Socket(host, 80);
        } catch (UnknownHostException e) {
            System.out.println("Host is not found. " + host);
            return;
        }
        String uriPath = "";
        if (uri.getPath() != null)
            uriPath += uri.getPath();
        else
            uriPath += "/";

        if (uri.getQuery() != null)
            uriPath += "?" + uri.getQuery();
        OutputStream socketOutputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(socketOutputStream);

        writer.println("GET " + uriPath + " HTTP/1.0");

        writer.print("Host: " + host + NewLineCharacter);

        if (getHelper.isHttpHeader()) {
            for (Map.Entry<String, String> headers :
                    getHelper.getHeaderValue().entrySet()) {
                writer.print(headers.getKey() + ":" + headers.getValue() + NewLineCharacter);
            }
        }
        
        if (getHelper.isInlineData()) {
            if (getHelper.getInlineData().contains("'")) {
            	getHelper.setInlineData(getHelper.getInlineData().replace("'", ""));
            }
            if (getHelper.getInlineData().equals("\"")) {
            	getHelper.setInlineData(getHelper.getInlineData().replace("'\"", ""));
            }
            writer.print("Content-Length: " + getHelper.getInlineData()
                    .length() + NewLineCharacter);
        } 

        writer.print(NewLineCharacter);
        writer.flush();

        BufferedReader getResponseFromSocket =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseStatus = getResponseFromSocket.readLine();
        getResponseFromSocket.mark(1000);
        if (getHelper.isFileWrite()) {
            printInFile(getResponseFromSocket, responseStatus, getHelper);
        } else {
            printOnConsole(getResponseFromSocket, responseStatus, getHelper);
        }

        //Redirect
        String[] responseStatusArray = responseStatus.split(" ");
        getResponseFromSocket.reset();
        if (responseStatusArray[1].startsWith("3")) {
            String response;
            List<String> redirectParameters = new ArrayList<>();
            while ((response = getResponseFromSocket.readLine()) != null) {
                if (response.startsWith("Location:")) {
                    redirectParameters.add(Httpc.HTTPC);
                    redirectParameters.add("get");
                    redirectParameters.add("-v");
                    redirectParameters.add("-h");
                    redirectParameters.add("Content-Type:text/html");
                    redirectParameters.add("-h");
                    redirectParameters.add("Keep-Alive:10");
                    redirectParameters.add("-h");
                    redirectParameters.add("Accept-language:en");
                    redirectParameters.add("http://" + host + "/" + response.split(" ")[1]);
                    break;
                }
            }
            get(redirectParameters);
        }

    }

    /**
     * This method executes post command
     * @param parameters Httpc command parameters
     * @throws IOException throws if there is an error in file reading or writing
     */
    public void post(List<String> parameters) throws IOException {
        StringBuilder fileData = null;
        HttpcHelper postHelper = new HttpcHelper();
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
                    System.out.println("Run 'httpc help or httpc help <Command>' for " +
                            "available " +
                            "httpc commands and options.");
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
                postHelper.setInlineData(parameters.get(i + 1));
                i++;
            } else if (parameters.get(i).equals("-f")) {
                postHelper.setFileSend(true);
                postHelper.setFileSendPath(parameters.get(i + 1));
                fileData = new StringBuilder();
//                System.out.println("HIi");
                i++;
            } else {
                System.out.println("Could Not Find an Option or FLag \"" + parameters.get(i) +
                        "\"");
                System.out.println();
                System.out.println("Run 'httpc help or httpc help <Command>' for available " +
                        "httpc commands and options.");
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
            socket = new Socket(host, 80);
        } catch (UnknownHostException e) {
            System.out.println("Host is not found. " + host);
            return;
        }
        String postPath = "";
        if (uri.getPath() != null) {
            postPath = postPath + uri.getPath();
        } else {
            postPath = "/";
        }

        OutputStream socketOutputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(socketOutputStream);

        writer.println("POST " + postPath + " HTTP/1.0");

        writer.print("Host: " + uri.getHost() + NewLineCharacter);

        if (postHelper.isHttpHeader()) {
            for (Map.Entry<String, String> headers :
                    postHelper.getHeaderValue().entrySet()) {
                writer.print(headers.getKey() + ":" + headers.getValue() + NewLineCharacter);
            }
        }

        if (postHelper.isInlineData()) {
            if (postHelper.getInlineData().contains("'")) {
                postHelper.setInlineData(postHelper.getInlineData().replace("'", ""));
            }
            if (postHelper.getInlineData().equals("\"")) {
                postHelper.setInlineData(postHelper.getInlineData().replace("'\"", ""));
            }
            writer.print("Content-Length: " + postHelper.getInlineData()
                    .length() + NewLineCharacter);
        } else if (postHelper.isFileSend()) {
            File dataFile = new File(postHelper.getFileSendPath());
            BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
            String tempFileData;
            while ((tempFileData = fileReader.readLine()) != null) {
                fileData.append(tempFileData);
            }
            writer.print("Content-Length: " + fileData.length() + NewLineCharacter);
            fileReader.close();
        }

        if (postHelper.isInlineData()) {
            writer.print(NewLineCharacter);
            writer.print(postHelper.getInlineData());
            writer.print(NewLineCharacter);
        } else if (postHelper.isFileSend()) {
            writer.print(NewLineCharacter);
            writer.print(fileData.toString());
            writer.print(NewLineCharacter);
        } else {
            writer.print(NewLineCharacter);
        }

        writer.flush();

        BufferedReader getResponseFromSocket =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseStatus = getResponseFromSocket.readLine();
        getResponseFromSocket.mark(1000);
        if (postHelper.isFileWrite()) {
            printInFile(getResponseFromSocket, responseStatus, postHelper);
        } else {
            printOnConsole(getResponseFromSocket, responseStatus, postHelper);
        }

        //Redirect
        String[] responseStatusArray = responseStatus.split(" ");
        getResponseFromSocket.reset();
        if (responseStatusArray[1].startsWith("3")) {
            String response;
            List<String> redirectParameters = new ArrayList<>();
            while ((response = getResponseFromSocket.readLine()) != null) {
                if (response.startsWith("Location:")) {
                    redirectParameters.add(Httpc.HTTPC);
                    redirectParameters.add("post");
                    redirectParameters.add("-v");
                    if(postHelper.isFileSend()){
                        redirectParameters.add("-f");
                        redirectParameters.add(postHelper.getFileSendPath());
                    }
                    if(postHelper.isInlineData()) {
                        redirectParameters.add("-d");
                        redirectParameters.add(postHelper.getInlineData());
                    }
                    redirectParameters.add("-h");
                    redirectParameters.add("Keep-Alive:10");
                    redirectParameters.add("-h");
                    redirectParameters.add("Accept-language:en");
                    redirectParameters.add("http://" + host + "/" + response.split(" ")[1]);
                    break;
                }
            }
            get(redirectParameters);
        }
    }
}
