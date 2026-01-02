package commands;

import service.CommandExecutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class EchoCommand implements Command {
    private String name;

    public EchoCommand() {
        name = "echo";
    }

    @Override
    public void execute(CommandExecutor executor, List<String> args) {
        StringBuilder string = new StringBuilder();
        StringBuilder directory = new StringBuilder();
        boolean writingToDirectory = false;
        boolean writingErrorToDirectory = false;
        boolean appendingToFile = false;
        for (int i = 1; i < args.size(); i++) {
            if (args.get(i).matches(">") || args.get(i).matches("1>")) {
                writingToDirectory = true;
                continue;
            }
            if (args.get(i).matches("1>>") || args.get(i).matches(">>")) {
                appendingToFile = true;
                continue;
            }
            if (args.get(i).matches("2>") || args.get(i).matches("2>>")) {
                writingErrorToDirectory = true;
                continue;
            }

            if (appendingToFile || writingToDirectory || writingErrorToDirectory) {
                directory.append(args.get(i));
                continue;
            }

            string.append(args.get(i));
            string.append(" ");
        }
        if (appendingToFile) {
            Path direct = Paths.get(directory.toString());
            try {
                String line = string.toString() + System.lineSeparator();
                Files.write(direct, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (writingErrorToDirectory) {
            Path direct = Paths.get(directory.toString());
            try {
                System.out.println(string);
                Files.write(direct, "".getBytes(StandardCharsets.UTF_8));
                return;
            } catch (Exception e) {
                try {
                    Files.write(direct, e.getMessage().getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        if (writingToDirectory) {
            Path direct = Paths.get(directory.toString());
            try {
                String line = string + System.lineSeparator();
                Files.write(direct, line.getBytes());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }
        System.out.println(string);

    }

    @Override
    public String getName() {
        return this.name;
    }
}
