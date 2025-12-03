import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();
            if (command.equals("exit")){
                break;
            }
            if (command.contains("echo")){
                System.out.println(command.substring(command.indexOf(" ") + 1));
                continue;
            }
            StringBuilder builder = new StringBuilder(command);
            builder.append(": command not found ");
            System.out.println(builder.toString());
        }



    }
}
