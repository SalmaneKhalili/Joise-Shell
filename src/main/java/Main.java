import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();
            StringBuilder builder = new StringBuilder(command);
            builder.append(": command not found ");
            System.out.println(builder.toString());
        }



    }
}
