package commands;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CdCommand implements Command{
    private String name;
    public CdCommand() {
        name = "cd";
    }

    public void execute(List<String> args){
        System.setProperty("user.dir", args.get(1));
    }
    @Override
    public String getName(){
        return this.name;
    }
    @Override
    public void execute(List<String> args, CommandExecutor executor){
        Path currentDirectory = executor.getCwd();
        if (Paths.get(args.get(1)).toFile().isDirectory()){
            executor.setCwd(currentDirectory.resolve(args.get(1)).normalize());
        } else {
            System.out.println("cd " + args.get(1)+": No such file or directory");
        }

    }
}
