package ua.hubanov.first;

import ua.hubanov.utils.TxtFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamWordCounter {

    public static final String ALL_WORDS_PATTERN = "[^a-zA-Z]";
    public static final String TXT_FILE_EXTENSION = ".txt";
    public static final String KEY_VALUE_SEPARATOR = " : ";
    private static final Map<String, Pattern> mapOfPatterns = new HashMap<>();

    static {
        mapOfPatterns.put("a-g", Pattern.compile("^[a-g]"));
        mapOfPatterns.put("h-n", Pattern.compile("^[h-n]"));
        mapOfPatterns.put("o-u", Pattern.compile("^[o-u]"));
        mapOfPatterns.put("v-z", Pattern.compile("^[v-z]"));
    }

    public static void process(String path) throws IOException {
        long start = System.currentTimeMillis();

        List<Path> filesList = getFilesList(path);
        Map<String, Long> countedWords = countWords(filesList);

        Map<String, Map<String, Long>> splittedAndSortedWords = splitMapOfWords(countedWords);
        writeToFiles(splittedAndSortedWords);

        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("StreamWordCounter finished process in " + elapsedTime + "millis");
    }

    private static List<Path> getFilesList(String path) {
        List<Path> filesList = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(path), new TxtFileFilter())) {
            directoryStream.forEach(filesList::add);
        } catch (IOException e) {
            System.out.println("Error while reading path: " + path);
        }
        return filesList;
    }

    private static Map<String, Long> countWords(List<Path> pathList) throws IOException {
        return pathList.stream()
                .flatMap(StreamWordCounter::getLinesFromFile)
                .flatMap(line -> Arrays.stream(line.trim().split(" ")))
                .map(word -> word.replaceAll(ALL_WORDS_PATTERN, "").toLowerCase().trim())
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private static Stream<String> getLinesFromFile(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            System.out.println("Error while reading from path: " + path);
        }
        return null;
    }

    private static Map<String, Map<String, Long>> splitMapOfWords(Map<String, Long> countedWords) {
        Map<String, Map<String, Long>> splittedMap = new HashMap<>();
        mapOfPatterns.forEach((key, value) -> splittedMap.put(key, filterWordsByPattern(value, countedWords)));
        return splittedMap;
    }

    private static Map<String, Long> filterWordsByPattern(Pattern pattern, Map<String, Long> countedWords) {
        return countedWords.entrySet().stream()
                .filter(e -> pattern.matcher(e.getKey()).find())
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private static void writeToFiles(Map<String, Map<String, Long>> map) {
        String path = "D:/firstResult/";
        map.forEach((fileName, wordsMap) -> write(path, fileName, wordsMap));
    }

    private static void write(String path, String fileName, Map<String, Long> map) {
        try {
            createFoldersAndFilesIfNotExist(path, fileName);

            Files.write(Paths.get(path + fileName + TXT_FILE_EXTENSION), () -> map.entrySet().stream()
                    .<CharSequence>map(e -> e.getKey() + KEY_VALUE_SEPARATOR + e.getValue())
                    .iterator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFoldersAndFilesIfNotExist(String path, String k) throws IOException {
        File file = new File(path, k + TXT_FILE_EXTENSION);
        if (!file.exists()) {
            Path storagePath = Paths.get(path + k + TXT_FILE_EXTENSION);
            Files.createDirectories(storagePath.getParent());
            Files.createFile(storagePath);
        }
    }
}
