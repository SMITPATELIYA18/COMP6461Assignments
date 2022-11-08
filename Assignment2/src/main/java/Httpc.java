//-------------------------------------------------
// Assignment 2
// © Smit Pateliya and Raviraj Savaliya
// Written by: Smit Pateliya (40202779) & Raviraj Savaliya (40200503)
//-------------------------------------------------

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * This class is driver for cURL like application
 */
public class Httpc {

    /**
     * This is a main command.
     */
    public static final String HTTPC = "httpc";

    /**
     * This is a main Method.
     *
     * @param args get from command line interface
     * @throws IOException throws if there is an error in file reading or writing
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String input;
        if (args.length == 0) {
            int flag = 0;
            System.out.println("******************************** WELCOME TO THE CURL " +
                    "APPLICATION ********************************");

            do {
                System.out.println("Enter the command or press 0 for exit");
                Scanner sc = new Scanner(System.in);
                input = sc.nextLine();
                if (input.equals("0")) {
                    flag = 1;
                } else {
                    List<String> input_split = Arrays.asList(input.split(" "));
                    checkCommand(input_split);
                }
            }
            while (flag != 1);
        } else {
            checkCommand(List.of(args));
        }
    }

    /**
     * This method differentiates help, get and post parameters
     *
     * @param parameters Httpc command parameters
     * @throws IOException throws if there is an error in file reading or writing
     */
    static void checkCommand(List<String> parameters) throws IOException,
            ClassNotFoundException {
        if (!parameters.get(0).equals(HTTPC)) {
            System.out.println("Command Not Found: " + parameters.get(0));
            return;
        }
        HttpcLibrary http_lib = new HttpcLibrary();
        if (parameters.size() == 1) {
            System.out.println("httpc is a curl-like application but supports HTTP protocol " +
                    "only.\n" +
                    "Usage: \n\thttpc command [arguments] \nThe commands are: \n\tget \t" +
                    "executes a " +
                    "HTTP GET request and prints the response.\n\tpost \texecutes a HTTP " +
                    "POST" +
                    "request and prints the response. \n\thelp \tprints this screen. \n\nUse" +
                    " " +
                    "\"httpc " +
                    "help [command]\" for more information about a command.");
        } else {
            switch (parameters.get(1)) {
                case "help":
                    http_lib.help(parameters);
                    break;
                case "get":
                    http_lib.get(parameters);
                    break;
                case "post":
                    http_lib.post(parameters);
                    break;
                default:
                    System.out.println("Could Not Find an Option or FLag \"" + parameters.get(1) +
                            "\"");
                    System.out.println();
                    System.out.println("Run 'httpc help or httpc help <Command>' for " +
                            "available " +
                            "httpc commands and options.");
                    break;
            }
        }

    }
}
