package ua.hubanov.second;

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

public class ParallelStreamWordCounter {

    private static final String TXT_FILE_EXTENSION = ".txt";
    private static final String KEY_VALUE_SEPARATOR = " : ";
    private static final String ALL_WORDS_PATTERN = "[^a-zA-Z]";
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
        System.out.println("ParallelStreamWordCounter finished process in " + elapsedTime + "millis");
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

    private static Map<String, Long> countWords(List<Path> pathList) {
        Map<String, Long> result = Collections.synchronizedMap(new HashMap<>());
        pathList.parallelStream()
                .map(ParallelStreamWordCounter::countWordsOfAFile)
                .forEach(map -> combineWordsToOneMap(map, result));
        return result;
    }

    private static void combineWordsToOneMap(Map<String, Long> input, Map<String, Long> result) {
        for (Map.Entry<String, Long> e : input.entrySet()) {
            result.put(e.getKey(), e.getValue() + result.getOrDefault(e.getKey(), 0L));
        }
    }

    private static Map<String, Long> countWordsOfAFile(Path path) {
        return getLinesFromFile(path)
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

    private static Map<String, Map<String, Long>> splitMapOfWords(Map<String, Long> countWords) {
        return mapOfPatterns.entrySet().parallelStream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                m -> filterAndSortByPattern(m.getValue(), countWords)));
    }

    private static Map<String, Long> filterAndSortByPattern(Pattern p, Map<String, Long> input) {
        return input.entrySet().stream()
                .filter(i -> p.matcher(i.getKey()).find())
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private static void writeToFiles(Map<String, Map<String, Long>> splittedAndSortedWords) {
        String path = "D:/secondResult/";
        splittedAndSortedWords.entrySet().parallelStream()
                        .forEach(map -> write(map, path));
    }

    private static void write(Map.Entry<String, Map<String, Long>> map, String path) {
        try {
            createFoldersAndFilesIfNotExist(path, map.getKey());

            Files.write(Paths.get(path + map.getKey() + TXT_FILE_EXTENSION), () -> map.getValue().entrySet().stream()
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
