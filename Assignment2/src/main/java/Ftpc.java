//-------------------------------------------------
// Assignment 2
// © Smit Pateliya and Raviraj Savaliya
// Written by: Smit Pateliya (40202779) & Raviraj Savaliya (40200503)
//-------------------------------------------------

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Ftpc {
    public static final String HTTPFS = "httpfs";

    public static void main(String args[]) throws ClassNotFoundException, IOException {
        String input;
        if (args.length == 0) {
            int flag = 0;
            do {
                System.out.print("Enter the File Transfer Command:");
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


    public static void checkCommand(List<String> parameters) throws IOException,
            ClassNotFoundException {
        FtpcLibrary ftp_lib = new FtpcLibrary();
        if (!parameters.get(0).equals(HTTPFS)) {
            System.out.println("Command Not Found:" + parameters.get(0));
            return;
        }
        switch (parameters.get(1)) {
            case "get":
                ftp_lib.get(parameters);
                break;
            case "post":
                ftp_lib.post(parameters);
                break;
            default:
                System.out.println("Could not find an Flag" + parameters.get(1) + "\"");
                break;
        }

    }
}
