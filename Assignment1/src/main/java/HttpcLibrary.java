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

public class HttpcLibrary {

    private final String NewLineCharacter = "\r\n";

    public HttpcLibrary() {
    }

    private static void printOnConsole(BufferedReader bufferedReader, String status,
                                       HttpcHelper getHelper) throws IOException {
        String line;
//        System.out.println("Sa"+status);
        if (getHelper.isVerbosePreset()) {
            System.out.println(status);
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } else {
            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(line);
                if (line.equals(""))
                    break;
            }
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

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
            while ((line = bufferedReader.readLine()) != null) {
                printWriter.println(line);
            }
        } else {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals(""))
                    break;
            }
            while ((line = bufferedReader.readLine()) != null) {
                printWriter.println(line);
            }
        }
        printWriter.println();
        System.out.println("Response has been written in " + getHelper.getFileWritePath() +
                ".");
        printWriter.flush();
        printWriter.close();

    }

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
                    "Associates headers to HTTP Request with the format 'key:value'.");
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
                    "the body HTTP POST request. \nEither [-d] or [-f] can be used but not " +
                    "both.");
            return;
        }
        System.out.println("Could Not Find an Option or FLag \"" + parameters.get(2) +
                "\"");
        System.out.println();
        System.out.println("Run 'httpc help or httpc help <Command>' for available " +
                "httpc commands and options.");
    }

    public void get(List<String> parameters) throws IOException {
//        System.out.println("----------------------------------"+parameters);
        HttpcHelper getHelper = new HttpcHelper();
        for (int i = 2; i < parameters.size(); i++) {
            if (parameters.get(i).equals("-v")) {
                getHelper.setVerbosePreset(true);
            } else if (parameters.get(i).equals("-h")) {
                HashMap<String, String> headerValue = getHelper.getHeaderValue();
//                System.out.println(parameters.get(i+1));
//                System.out.println(parameters.get(i + 1).contains(":"));
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
        Socket socket = null;
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
//        System.out.println(uriPath);
        OutputStream socketOutputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(socketOutputStream);

        writer.println("GET " + uriPath + " HTTP/1.0");

        writer.print("Host: " + host + NewLineCharacter);

        if (getHelper.isHttpHeader()) {
//            System.out.println("In Header");
            for (Map.Entry<String, String> headers :
                    getHelper.getHeaderValue().entrySet()) {
//                System.out.println(headers.getKey()+" "+headers.getValue());
                writer.print(headers.getKey() + ":" + headers.getValue() + NewLineCharacter);
            }
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
//                System.out.println("1");
                if (response.startsWith("Location:")) {
//                    System.out.println("HII");
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
//                System.out.println(parameters.get(i+1));
//                System.out.println(parameters.get(i + 1).contains(":"));
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
//        System.out.println(postHelper.getRequestURL() + "HIi");
        URI uri = null;
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
        Socket socket = null;
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
//            System.out.println("In Header");
            for (Map.Entry<String, String> headers :
                    postHelper.getHeaderValue().entrySet()) {
//                System.out.println(headers.getKey()+" "+headers.getValue());
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
            String tempFileData = null;
            while ((tempFileData = fileReader.readLine()) != null) {
                fileData.append(tempFileData);
            }
//            System.out.println(fileData.toString());
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
        if (postHelper.isFileWrite()) {
            printInFile(getResponseFromSocket, responseStatus, postHelper);
        } else {
            printOnConsole(getResponseFromSocket, responseStatus, postHelper);
        }
    }
}
