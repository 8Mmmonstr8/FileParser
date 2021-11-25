package ua.hubanov.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

public class TxtFileFilter implements DirectoryStream.Filter<Path> {
    @Override
    public boolean accept(Path entry) throws IOException {
        return entry.toString().endsWith(".txt");
    }
}
