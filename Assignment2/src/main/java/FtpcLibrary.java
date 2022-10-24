import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FtpcLibrary {
	
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
			while(flag !=1);
		}
		else {
			checkCommand(List.of(args));
			}
		}
		
	
	public static void checkCommand(List<String> parameters) throws IOException,ClassNotFoundException {
		
	}
}
