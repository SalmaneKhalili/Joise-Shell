package commands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class PathResolver {
    final String PATH;

    public PathResolver() {
        this.PATH = System.getenv("PATH");
    }

    public Optional<Path> findExecutable(String command) {
        Optional<Path> opt = Optional.empty();
        if (this.PATH != null) {
            String[] directories = PATH.split(File.pathSeparator);
            for (String directoryPath : directories) {
                Path path = Paths.get(directoryPath);
                if (Files.isDirectory(path)) {
                    try (Stream<Path> paths = Files.walk(path, 1)) {
                        Optional<Path> testCase = paths.filter(Files::isExecutable).filter(file -> file.getFileName().toString().equals(command)).findAny();
                        if (testCase.isPresent()){
                            return testCase;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return opt;
    }
}
