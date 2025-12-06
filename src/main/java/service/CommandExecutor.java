package service;

import commands.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CommandExecutor {
    private final Map<String, Command> builtInCommands = new HashMap<>();
    private final PathResolver pathResolver;
    private Path cwd;


    public CommandExecutor(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        cwd = Paths.get(".").toAbsolutePath().normalize();
        // Register built-in commands here
        registerCommand(new ExitCommand());
        registerCommand(new EchoCommand());
        registerCommand(new TypeCommand());
        registerCommand(new PwdCommand());
        registerCommand(new CdCommand());
    }

    public Path getCwd() {
        return this.cwd;
    }

    public void setCwd(Path path) {
        this.cwd = path;
    }

    private void registerCommand(Command command) {
        builtInCommands.put(command.getName(), command);
    }

    public static List<String> parse(String line) {
        List<String> commandLine = new ArrayList<>();
        boolean quotedMode = false;
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ' && commandLine.size() == 0) {
                commandLine.add(builder.toString());
                builder = new StringBuilder("");
                continue;
            }
            if (line.charAt(i) == '\\') {

                continue;
            }
            if (line.charAt(i) == '\'' && line.charAt(i - 1) == '\\') {
                builder.append(line.charAt(i));
                continue;
            }
            if (line.charAt(i) == '\'') {
                quotedMode = !quotedMode;
                continue;
            }
            if (quotedMode) {
                builder.append(line.charAt(i));
                continue;
            }
            if (line.charAt(i) == ' ' && line.charAt(i - 1) == ' ') {
                continue;
            }
            builder.append(line.charAt(i));

        }
        commandLine.add(builder.toString());
        return commandLine;
    }

    public boolean executeCommand(String commandLine) {
        List<String> commandAndArguments = parse(commandLine);


        // 1. Split the commandLine into parts
        //List<String> commandSplit = List.of(commandLine.split(" "));
        // 2. Extract the command name
        String commandName = commandAndArguments.get(0);
        // 3. Check builtInCommands map
        // 4. If found, call command.execute(args) and return true
        if (builtInCommands.containsKey(commandName)) {
            if (commandName.equals("type") && this.builtInCommands.containsKey(commandAndArguments.get(1))) { //for builtin commands
                System.out.println(commandAndArguments.get(1) + " is a shell builtin");
                return true;
            }

            builtInCommands.get(commandName).execute(commandAndArguments, this);
            return true;
        }
        // 5. If not found, use pathResolver to find the external program
        Optional<Path> executablePath = pathResolver.findExecutable(commandName);
        // 6. If external program is found, execute it
        if (executablePath.isPresent()) {
            List<String> commandAndArgs = new ArrayList<>();
            commandAndArgs.add(executablePath.get().getFileName().toString());
            if (commandAndArguments.size() > 1) {
                commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.size()));
            }
            ProcessBuilder builder = new ProcessBuilder(commandAndArgs);
            builder.directory(cwd.toFile());
            try {
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                while ((line = errReader.readLine()) != null) {
                    System.out.println(line);
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.out.println(commandName + ": command not found");
        }
        return true;
        // 7. If neither, print "command not found" and return true (to continue loop)
        // 8. Special handling for 'exit'
    }
}