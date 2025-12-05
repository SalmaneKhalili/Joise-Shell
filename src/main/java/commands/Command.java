package commands;

import java.util.List;

public interface Command {
    // A command needs to execute based on the command parts (arguments)
    void execute(List<String> args, CommandExecutor executor);
    String getName();
}
