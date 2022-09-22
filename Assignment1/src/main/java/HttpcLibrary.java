import java.util.List;

public class HttpcLibrary {

    public HttpcLibrary() {}

    public void help(List<String> parameters) {
        if(parameters.size() == 2) {
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
        if(parameters.get(2).equals("get")) {
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
}
