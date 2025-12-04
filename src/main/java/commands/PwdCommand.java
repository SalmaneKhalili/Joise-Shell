package commands;

import java.nio.file.Paths;
import java.util.List;

public class PwdCommand implements Command {
    private String name;
    public PwdCommand(){
        this.name = "pwd";
    }


    @Override
    public void execute(List<String> args){
        String directory = Paths.get("").toAbsolutePath().toString();
        System.out.println(directory);
    }
    @Override
    public String getName(){
        return this.name;
    }
}
