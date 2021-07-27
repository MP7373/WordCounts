package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class WordCounts {

    private static final Map<String, Integer> wordCounts = new HashMap<>();

    private static final List<String> allWords = new ArrayList<>();

    private static final int numberOfWordsToPrint = 25;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("You must pass a url as the only argument to this program.");
            return;
        }
        String url = args[0];
        Document document = Jsoup.connect(url).get();

        // Going element by element using element.ownText() gave me better results
        // than just using document.body().text()
        processNodeTree(document.body());

        // I implemented a quick select because it has linear time complexity
        // to solve "find the top x elements of a list" by some comparison criteria.
        // Sorting the entire list is n log n, which is slower.
        // If you're not familiar, quick select basically just quick sort where you only
        // tunnel down one side and you can ignore the other side because you only care
        // about finding the top x elements not making sure the entire list is sorted.
        // So if you only care about the top 25 elements and you know that more than
        // 25 elements are greater than your chosen pivot, you can ignore everything
        // smaller than the chosen pivot, whereas in quick sort you would still need
        // to make sure the smaller elements are sorted and always need to keep
        // processing both sides of the list.
        // The requirements only say to get the top 25 words, not that the
        // top 25 most occurring words themselves must also be sorted so
        // I don't sort the top 25 I just print them in what ever order they
        // ended up after the quick select.
        // This is really a micro optimization and even testing with a site
        // with large amounts of text (https://www.longestjokeintheworld.com/)
        // it was only ~1-4 ish milliseconds faster than simply sorting
        // the list with the default List.sort() method (I did time the sorting
        // in isolation to confirm this, as the variation on response time is
        // more than is gained by this).
        // I was at least happy that it was slightly faster than sorting with the
        // standard library method so I left it in, but in practice
        // on a team I would probably favor sorting because of it's simplicity
        // and maintainability (also the performance gain is negligible).
        partitionHighCountWordsToLeft(numberOfWordsToPrint, allWords.size() - 1);

        // Alternatively you could just sort like this for a much simpler implementation
        //allWords.sort(Comparator.comparingInt(word -> wordCounts.get(word)).reversed());

        int countOfWordsToPrint = Math.min(numberOfWordsToPrint, allWords.size());
        for (int i = 0; i < countOfWordsToPrint; i++) {
            System.out.println(allWords.get(i) + " - times on page: " + wordCounts.get(allWords.get(i)));
        }
    }

    private static void processNodeTree(Element node) {
        String nodeText = node.ownText();
        addWordsFromStringToWordCountsMap(nodeText);
        for (Element child : node.children()) {
            processNodeTree(child);
        }
    }

    private static void addWordsFromStringToWordCountsMap(String s) {
        String[] words = s.split("\\s");
        for (String word : words) {
            // I'm making an assumption that the language is only english and understand
            // that characters like apostrophes and periods, as well as non a-z character
            // words will not be included
            String processedWord = word.toLowerCase()
                    .replaceAll("\\W|\\d|-|_", "");
            if (processedWord.length() > 0) {
                if (!wordCounts.containsKey(processedWord)) {
                    allWords.add(processedWord);
                }
                wordCounts.put(processedWord, wordCounts.getOrDefault(processedWord, 0) + 1);
            }
        }
    }

    private static void partitionHighCountWordsToLeft(int numberOfElementsToPartition, int listSize) {
        if (numberOfElementsToPartition >= allWords.size()) {
            return;
        }
        int everythingToLeftIsLargerIndex = 0;
        int everythingToRightSmallerIndex = listSize - 1;

        while (true) {
            int passStartLeftBound = everythingToLeftIsLargerIndex;
            int passStartRightBound = everythingToRightSmallerIndex;

            int pivotWordCount = wordCounts.get(allWords.get(everythingToLeftIsLargerIndex));
            while (everythingToLeftIsLargerIndex < everythingToRightSmallerIndex) {
                int oneToRightIndex = everythingToLeftIsLargerIndex + 1;
                int oneToRightWordCount = wordCounts.get(allWords.get(oneToRightIndex));
                if (pivotWordCount < oneToRightWordCount) {
                    Collections.swap(allWords, everythingToLeftIsLargerIndex++, oneToRightIndex);
                } else {
                    Collections.swap(allWords, oneToRightIndex, everythingToRightSmallerIndex--);
                }
            }

            if (everythingToLeftIsLargerIndex == numberOfElementsToPartition) {
                return;
            } else if (everythingToLeftIsLargerIndex > numberOfElementsToPartition) {
                everythingToLeftIsLargerIndex = passStartLeftBound;
                everythingToRightSmallerIndex = passStartRightBound - 1;
            } else {
                everythingToLeftIsLargerIndex++;
                everythingToRightSmallerIndex = passStartRightBound;
            }
        }
    }
}
