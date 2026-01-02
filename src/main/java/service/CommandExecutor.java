package service;

import commands.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        ParsingState currentParsingState = ParsingState.DEFAULT;
        ParsingState previousParsingState = ParsingState.DEFAULT;
        CharStream stream = new CharStream(line);
        StringBuilder currentWord = new StringBuilder();

        while (stream.hasNext()) {
            char c = stream.next();
            switch (currentParsingState) {
                case DEFAULT:
                    if (c == '"') {
                        currentParsingState = ParsingState.IN_DOUBLE_QUOTES;
                        previousParsingState = ParsingState.DEFAULT;
                        break;
                    }
                    if (c == '\'') {
                        currentParsingState = ParsingState.IN_SINGLE_QUOTES;
                        previousParsingState = ParsingState.DEFAULT;
                        break;
                    }
                    if (c == '\\') {
                        currentParsingState = ParsingState.ESCAPED;
                        previousParsingState = ParsingState.DEFAULT;
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
                    if (c == '\"') {
                        currentParsingState = ParsingState.DEFAULT;
                        break;
                    }
                    if (c == '\\') {
                        char nextChar = stream.peek();
                        if (nextChar == '"' || nextChar == '$' || nextChar == '`' || nextChar == '\\') {
                            currentParsingState = ParsingState.ESCAPED;
                            previousParsingState = ParsingState.IN_DOUBLE_QUOTES;
                            break;
                        }

                    }
                    currentWord.append(c);
                    break;
                case IN_SINGLE_QUOTES:
                    if (c == '\'') {
                        currentParsingState = ParsingState.DEFAULT;
                        break;
                    }
                    currentWord.append(c);
                    break;
                case ESCAPED:
                    currentWord.append(c);
                    currentParsingState = previousParsingState;
                    break;
            }


        }
        commandAndArguments.add(currentWord.toString());

        return commandAndArguments;


    }

    public boolean executeCommand(String commandLine) {
        List<String> commandAndArguments = parse(commandLine);
        String commandName = commandAndArguments.getFirst();
        WritingState writingState = WritingState.DEFAULT;
        if (isBuiltInCommand(commandName, commandAndArguments)) return true;
        Optional<Path> executablePath = pathResolver.findExecutable(commandName);

        if (executablePath.isEmpty()) {
            System.out.println(commandName + ": command not found");
            return true;
        }

        List<String> commandAndArgs = new ArrayList<>();
        commandAndArgs.add(executablePath.get().getFileName().toString());
        ProcessRecord processRecord = getPath(commandAndArguments, commandAndArgs, writingState);
        processor(processRecord.commandsAndArgument(), processRecord.filePath(), processRecord.writingState());
        return true;
    }

    private static ProcessRecord getPath(List<String> commandAndArguments, List<String> commandAndArgs, WritingState writingState) {

        if (commandAndArguments.contains(">>")) {
            commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.indexOf(">>")));
            Path filePath = Paths.get(commandAndArguments.get(commandAndArguments.indexOf(">>") + 1));
            return new ProcessRecord(commandAndArgs, filePath, WritingState.APPENDINGSTDOUT);
        }

        if (commandAndArguments.contains("1>>")) {
            commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.indexOf("1>>")));
            Path filePath = Paths.get(commandAndArguments.get(commandAndArguments.indexOf("1>>") + 1));
            return new ProcessRecord(commandAndArgs, filePath, WritingState.APPENDINGSTDOUT);
        }

        if (commandAndArguments.contains("2>>")) {
            commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.indexOf("2>>")));
            Path filePath = Paths.get(commandAndArguments.get(commandAndArguments.indexOf("2>>") + 1));
            return new ProcessRecord(commandAndArgs, filePath, WritingState.APPENDINGERROUT);
        }

        if (commandAndArguments.contains("2>")) {
            commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.indexOf("2>")));
            Path filePath = Paths.get(commandAndArguments.get(commandAndArguments.indexOf("2>") + 1));
            return new ProcessRecord(commandAndArgs, filePath, WritingState.REDIRECTERROUT);
        }

        if (commandAndArguments.contains(">")) {
            commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.indexOf(">"))); // split string into elements
            Path filePath = Paths.get(commandAndArguments.get(commandAndArguments.indexOf(">") + 1)); // isolationg the file path
            return new ProcessRecord(commandAndArgs, filePath, WritingState.REDIREDTSTDOUT);
        }

        if (commandAndArguments.contains("1>")) {
            commandAndArgs.addAll(commandAndArguments.subList(1, commandAndArguments.indexOf("1>")));
            Path filePath = Paths.get(commandAndArguments.get(commandAndArguments.indexOf("1>") + 1));
            return new ProcessRecord(commandAndArgs, filePath, WritingState.REDIREDTSTDOUT);
        }

        return new ProcessRecord(commandAndArguments, null, writingState);
    }

    public boolean isBuiltInCommand(String command, List<String> args) {
        if (!builtInCommands.containsKey(command)) {
            return false;
        }

        if (command.equals("type") && this.builtInCommands.containsKey(args.get(1))) { //for builtin commands
            System.out.println(args.get(1) + " is a shell builtin");
            return true;
        }

        builtInCommands.get(command).execute(this, args);
        return true;
    }

    public void processor(List<String> commandAndArgs, Path file, WritingState writingState) {
        ProcessBuilder builder = new ProcessBuilder(commandAndArgs);
        builder.directory(cwd.toFile());
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;


        while (true) {
            try {
                if ((line = reader.readLine()) == null) break; // break case
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (writingState == WritingState.DEFAULT || writingState == WritingState.REDIRECTERROUT || writingState == WritingState.APPENDINGERROUT) {
                System.out.println(line);
                continue;
            }
            if (writingState == WritingState.REDIREDTSTDOUT || writingState == WritingState.APPENDINGSTDOUT) {
                try {
                    String content = line + System.lineSeparator();
                    Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        while (true) {
            try {
                if ((line = errReader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (writingState == WritingState.DEFAULT || writingState == WritingState.REDIREDTSTDOUT) {
                System.out.println(line);
                continue;
            }
            if (writingState == WritingState.APPENDINGSTDOUT) {
                try {
                    System.out.println(line);
                    Files.createFile(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (writingState == WritingState.REDIRECTERROUT || writingState == WritingState.APPENDINGERROUT) {
                try {
                    String content = line + System.lineSeparator();
                    Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}