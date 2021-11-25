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
                .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase().trim())
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
        Map<String, Map<String, Long>> splittedMap = new HashMap<>();
        Pattern pag = Pattern.compile("^[a-g]");
        Pattern phn = Pattern.compile("^[h-n]");
        Pattern pou = Pattern.compile("^[o-u]");
        Pattern pvz = Pattern.compile("^[v-z]");
        splittedMap.put("a-g", countWords.entrySet().stream()
                .filter(e -> pag.matcher(e.getKey()).find())
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
        splittedMap.put("h-n", countWords.entrySet().stream()
                .filter(e -> phn.matcher(e.getKey()).find())
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
        splittedMap.put("o-u", countWords.entrySet().stream()
                .filter(e -> pou.matcher(e.getKey()).find())
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
        splittedMap.put("v-z", countWords.entrySet().stream()
                .filter(e -> pvz.matcher(e.getKey()).find())
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
        return splittedMap;
    }

    private static void writeToFiles(Map<String, Map<String, Long>> splittedAndSortedWords) {
        String path = "D:/firstResult/";
        splittedAndSortedWords.forEach((k,v) ->
                {
                    try {
                        File file = new File(path, k + ".txt");
                        if (!file.exists()) {
                            Path storagePath = Paths.get(path + k + ".txt");
                            Files.createDirectories(storagePath.getParent());
                            Files.createFile(storagePath);
                        }

                        Files.write(Paths.get(path + k + ".txt"), () -> v.entrySet().stream()
                                .<CharSequence>map(e -> e.getKey() + " : " + e.getValue())
                                .iterator());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}
