package service;

import java.nio.file.Path;
import java.util.List;

public record ProcessRecord(List<String> commandsAndArgument, Path filePath, WritingState writingState) {
}
