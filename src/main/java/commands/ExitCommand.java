package commands;

import java.util.List;

public class ExitCommand implements Command {
    private String name;
    public ExitCommand() {
        this.name = "exit";
    }
    @Override
    public void execute(List<String> args, CommandExecutor executor){
        System.exit(0);

    }

    @Override
    public String getName(){
        return this.name;
    }
}
