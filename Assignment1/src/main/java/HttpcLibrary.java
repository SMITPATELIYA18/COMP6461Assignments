import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpcLibrary {

    private String NewLineCharacter = "\r\n";

    public HttpcLibrary() {
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
        System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file]" +
                " URL \nPost executes a HTTP POST request for a given URL with inline data " +
                "or from file. \n\t-v Prints the detail of the response such as protocol, " +
                "status, and headers. \n\t-h key:value Associates headers to HTTP Request " +
                "with the format 'key:value'. \n\t-d string Associates an inline data to the" +
                " body HTTP POST request. \n\t-f file Associates the content of a file to " +
                "the body HTTP POST request. \n\tEither [-d] or [-f] can be used but not " +
                "both.");
    }

    public void get(List<String> parameters) throws URISyntaxException, IOException {
        HttpcHelper getHelper = new HttpcHelper();
        HashMap<String, String> headerValue = new HashMap<>();
        for (int i = 2; i < parameters.size(); i++) {
            if(parameters.get(i).equals("-v")) {
                getHelper.setVerbosePreset(true);
            } else if(parameters.get(i).equals("-h")) {
                if (parameters.get(i + 1).contains(":")) {
                    System.out.println("Please, Enter correct command!!!");
                    return;
                }
                String[] headerValues = parameters.get(i + 1).split(":");
                headerValue.put(headerValues[0], headerValues[1]);
                getHelper.setHeaderValue(headerValue);
            } else if(parameters.get(i).startsWith("http://") || parameters.get(i).startsWith("https://")) {
                getHelper.setRequestURL(parameters.get(i));
            } else if(parameters.get(i).equals("-o")) {
                getHelper.setFileWrite(true);
                getHelper.setFileWritePath(parameters.get(i+1));
            }
        }

        URI uri = new URI(getHelper.getRequestURL());
        System.out.println("url print "+ uri);
        String host = uri.getHost();
//        System.out.println(host);
        Socket socket = new Socket(host, 80);
        String uriPath = "";
        if(uri.getHost() != null)
            uriPath += uri.getPath();
        else
            uriPath +="/";

        if(uri.getQuery() != null)
            uriPath+= uri.getQuery();
//        System.out.println(uriPath);
        OutputStream socketOutputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(socketOutputStream);

        writer.println("GET " + uriPath + " HTTP/1.0");

        writer.print("Host: " + host + NewLineCharacter);

        if(getHelper.isHttpHeader()) {
            for (Map.Entry<String, String> headers:
                    getHelper.getHeaderValue().entrySet()) {
                writer.write(headers.getKey()+":"+headers.getValue()+NewLineCharacter);
            }
        }

        writer.print(NewLineCharacter);
        writer.flush();

        BufferedReader getResponseFromSocket =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printOnConsole(getResponseFromSocket, getResponseFromSocket.readLine(), getHelper);

    }

    public void post(List<String> parameters) {

    }

    private static void printOnConsole(BufferedReader bufferedReader, String status,
                                       HttpcHelper getHelper) throws IOException {
        String line = null;
//        System.out.println("Sa"+status);
        if(getHelper.isVerbosePreset()) {
            System.out.println(status);
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                if (line.equals("}"))
                    break;
            }
        } else {
//            System.out.println("hii");
            boolean isJson = false;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
//                if (line.trim().equals("{"))
//                    isJson = true;
//                if (isJson) {
//                    System.out.println(line);
//                    if (line.equals("}"))
//                        break;
//                }
            }
        }
    }
}
