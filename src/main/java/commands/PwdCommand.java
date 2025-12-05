package commands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PwdCommand implements Command {
    private String name;
    public PwdCommand(){
        this.name = "pwd";
    }



    public void execute(List<String> args){
        System.out.println(System.getProperty("user.dir"));
    }
    @Override
    public void execute(List<String> args, CommandExecutor executor){
        Path currentDirectory = executor.getCwd();
        System.out.println(currentDirectory);

    }
    @Override
    public String getName(){
        return this.name;
    }
}
