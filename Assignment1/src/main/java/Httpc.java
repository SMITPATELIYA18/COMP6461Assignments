import java.util.*;
import HttpcLibrary;
public class Httpc {
	public static void main(String args[]) {
		String input;
		//ArrayList<String> input_data = new ArrayList<>();
		HttpcLibrary http_lib = new HttpcLibrary();
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
					http_lib.validate(input_split);
				}
			}
			while(flag !=1);
		}
		else {
			List<String> input_split = Arrays.asList(input.split(" "));
			http_lib.validate(input_split);
		}
	}
}
