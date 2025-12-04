package commands;

import java.util.List;

public class EchoCommand implements Command {
    private String name;
    public EchoCommand() {
        name = "echo";
    }
    @Override
    public void execute(List<String> args){
        StringBuilder string = new StringBuilder("");
        for (int i = 1; i < args.size(); i++){
            string.append(args.get(i));
            string.append(" ");
        }
        System.out.println(string);

    }

    @Override
    public String getName(){
        return this.name;
    }
}
