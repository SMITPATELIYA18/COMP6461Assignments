import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Httpc {

	private static final String HTTPC = "httpc";

	public static void main(String args[]) {
		String input;
		//ArrayList<String> input_data = new ArrayList<>()
		if (args.length == 0) {
			int flag = 0 ;
			System.out.println("******************************** WELCOME TO THE CURL APPLICATION ********************************");
			
			do {
				System.out.println("Enter the command or press 0 for exit");
				Scanner sc = new Scanner(System.in);
				input = sc.nextLine();
				if(input.equals("0")) {
					flag = 1 ;
				}
				else {
					List<String> input_split = Arrays.asList(input.split(" "));
					checkCommand(input_split);
				}
			}
			while(flag !=1);
		}
		else {
			input = args.toString();
			List<String> input_split = Arrays.asList(input.split(" "));
			checkCommand(input_split);
		}
	}

	static void checkCommand(List<String> parameters) {
		if(!parameters.get(0).equals(HTTPC)) {
			System.out.println("Please, Enter correct command!!!");
			return;
		}
		HttpcLibrary http_lib = new HttpcLibrary();
		if(parameters.size() == 1) {
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
			if(parameters.get(1).equals("help")) {
//			System.out.println("Please, Check your command!!!");
				http_lib.help(parameters);
			}
		}

	}
}
