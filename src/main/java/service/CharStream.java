package service;

public class CharStream {
    private String input;
    private int position;

    public CharStream(String input) {
        this.input = input;
        this.position = 0;
    }
    public boolean hasNext(){
        return position < input.length();
    }
    public char peek() {
        if (hasNext()) {
            return input.charAt(position + 1);
        }
        return '0';

    }
    public char next() {
        if (hasNext()) {
            char nextChar = input.charAt(position); // 1. Read character at current position (e.g., index 0)
            position++;                             // 2. Advance to the next position (e.g., index 1)
            return nextChar;
        }
        return '0';
    }
    public void consumeNext() {
        position++;
    }
}
