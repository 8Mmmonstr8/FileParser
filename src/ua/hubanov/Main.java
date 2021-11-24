package ua.hubanov;

import ua.hubanov.first.StreamWordCounter;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        String path = "D:/";

        StreamWordCounter.process(path);

    }
}
