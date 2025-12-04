import commands.PathResolver;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PathResolver resolver = new PathResolver();
        CommandExecutor executor = new CommandExecutor(resolver);
        // We'll skip a dedicated 'Cli' class for brevity, and keep the main loop here for now

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();

            // The executor handles all the complexity
            boolean shouldContinue = executor.executeCommand(command);
            if (!shouldContinue) {
                break;
            }
        }
    }
}