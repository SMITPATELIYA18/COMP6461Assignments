import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Client {
    public final String HTTPFS = "httpfs";
    public static void main(String[] args) {
        Client client = new Client();
        String input;
        if (args.length == 0) {
            int flag = 0;
            do {
                System.out.print("Enter the File Transfer Command (0 for Exit): ");
                Scanner sc = new Scanner(System.in);
                input = sc.nextLine();
                if (input.equals("0")) {
                    flag = 1;
                } else {
                    client.checkCommand(input);
                }
            }
            while (flag != 1);
        } else {
            client.checkCommand(String.join(" ",args));
        }
    }

    private void checkCommand(String query) {
        List<String> parameters = Arrays.asList(query.split(" "));
        String url,postData = null;
        if (parameters.contains("-d") && parameters.contains("-f")) {
            System.out.println("Have entered '-d' and '-f in a command.'");
            System.out.println();
            return;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).equals(HTTPFS) && i==0) {

            } else if ((parameters.get(i).equals("get") || parameters.get(i).equals("post")) && i==1) {

            } else if (parameters.get(i).startsWith("http://")) {
                url = parameters.get(i);
            } else if (parameters.get(i).equals("-h")) {
                if (!parameters.get(i+1).contains(":")) {
                    System.out.println("There is problem with 'Headers'");
                    System.out.println();
                    return;
                }
                i++;
            } else if (parameters.get(i).equals("-d")) {
                String tempData = parameters.get(i+1);
                if (tempData.contains("'")) {
                    postData = tempData.replace("'", "");
                }
                if (tempData.equals("\"")) {
                    postData = tempData.replace("'\"", "");
                }
                i++;
            } else if (parameters.get(i).equals("-f")) {
                StringBuilder fileData = new StringBuilder();
                try {
                    File dataFile = new File(parameters.get(i+1));
                    BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
                    String tempFileData;
                    while ((tempFileData = fileReader.readLine()) != null) {
                        fileData.append(tempFileData);
                    }
                    if (fileData.toString().contains("'")) {
                        postData = fileData.toString().replace("'", "");
                    }
                    if (fileData.toString().equals("\"")) {
                        postData = fileData.toString().replace("'\"", "");
                    }
                    fileReader.close();
                } catch (Exception e) {
                    System.out.println("Enter Correct File");
                    return;
                }
                i++;
            }
        }
        query = "";
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).equals("-d")) {
                query+="-d ";
                query+= postData+" ";
                i++;
            } else if(parameters.get(i).equals("-f")) {
                query+="-d ";
                query+= postData+" ";
                i++;
            } else {
                query+=parameters.get(i)+" ";
            }
        }
        query = query.trim();
    }
}
