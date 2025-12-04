package commands;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TypeCommand implements Command {
    private String name;
    private PathResolver resolver;

    public TypeCommand(){
        this.name = "type";
        this.resolver = new PathResolver();
    }
    @Override
    public void execute(List<String> args){

        Optional<Path> path = resolver.findExecutable(args.get(1));
        if (path.isPresent()){
            System.out.println(args.get(1) + " is " + path.get().getParent() + "/"+path.get().getFileName());
        } else {
            System.out.println(args.get(1)+": not found");
        }
    }

    @Override
    public String getName(){
        return this.name;
    }
}
