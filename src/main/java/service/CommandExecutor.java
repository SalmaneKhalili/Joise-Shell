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
        List<String> commandAndArguments = new ArrayList<>();
        State currentState = State.DEFAULT;
        State previousState = State.DEFAULT;
        CharStream stream = new CharStream(line);
        StringBuilder currentWord = new StringBuilder();

        while(stream.hasNext()){
            char c = stream.next();
            switch (currentState) {
                case DEFAULT :
                    if (c == '"'){
                        currentState = State.IN_DOUBLE_QUOTES;
                        previousState = State.DEFAULT;
                        break;
                    }
                    if (c == '\''){
                        currentState = State.IN_SINGLE_QUOTES;
                        previousState = State.DEFAULT;
                        break;
                    }
                    if (c == '\\'){
                        currentState = State.ESCAPED;
                        previousState = State.DEFAULT;
                        break;
                    }

                    if (c == ' ') {
                        if (!currentWord.isEmpty()) {
                            commandAndArguments.add(currentWord.toString());
                            currentWord.setLength(0);
                        }
                        break;
                    }
                    currentWord.append(c);
                    break;
                case IN_DOUBLE_QUOTES:
                    if (c == '\"'){
                        currentState = State.DEFAULT;
                        break;
                    }
                    if (c == '\\'){
                        char nextChar = stream.peek();
                        if (nextChar == '"' || nextChar == '$' || nextChar == '`' || nextChar == '\\' ){
                            currentState = State.ESCAPED;
                            previousState = State.IN_DOUBLE_QUOTES;
                            break;
                        }

                    }
                    currentWord.append(c);

                    break;
                case IN_SINGLE_QUOTES:
                    if (c == '\'') {
                        currentState = State.DEFAULT;
                        break;
                    }
                    currentWord.append(c);
                    break;
                case ESCAPED:
                    currentWord.append(c);
                    currentState = previousState;
                    break;
            }


        }
        commandAndArguments.add(currentWord.toString());
        return commandAndArguments;



    }
    public boolean executeCommand(String commandLine) {
        List<String> commandAndArguments = parse(commandLine);
        String commandName = commandAndArguments.get(0);
        if (builtInCommands.containsKey(commandName)) {
            if (commandName.equals("type") && this.builtInCommands.containsKey(commandAndArguments.get(1))) { //for builtin commands
                System.out.println(commandAndArguments.get(1) + " is a shell builtin");
                return true;
            }

            builtInCommands.get(commandName).execute(commandAndArguments, this);
            return true;
        }

        Optional<Path> executablePath = pathResolver.findExecutable(commandName);

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
    }
}