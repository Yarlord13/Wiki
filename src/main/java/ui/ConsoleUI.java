package ui;

import browser.BrowserLauncher;
import domain.SearchResult;
import util.WikiRequest;
import util.WikiRequestException;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private static final int RESULTS_LIMIT = 10;
    private static final String EXIT_COMMAND = "exit";

    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Поиск в Википедии ===");
        System.out.println("Введите запрос или '" + EXIT_COMMAND + "' для выхода.");

        while (true) {
            String query = readQuery();
            if (query == null) {
                return;
            }

            List<SearchResult> results = search(query);
            if (results == null) {
                continue;
            }
            if (results.isEmpty()) {
                System.out.println("По запросу ничего не найдено.");
                continue;
            }

            printResults(results);
            handleSelection(results);
        }
    }

    private String readQuery() {
        while (true) {
            System.out.print("\nЗапрос: ");
            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase(EXIT_COMMAND)) {
                System.out.println("До свидания!");
                return null;
            }
            if (line.isEmpty()) {
                System.out.println("Запрос не может быть пустым. Попробуйте снова.");
                continue;
            }
            return line;
        }
    }

    private List<SearchResult> search(String query) {
        try {
            List<SearchResult> results =
                    WikiRequest.request(query, RESULTS_LIMIT).getQuery().getSearch();
            return results == null ? List.of() : results;

        } catch (WikiRequestException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return null;

        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return null;
        }
    }

    private void printResults(List<SearchResult> results) {
        System.out.println("\nНайдено статей: " + results.size());
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, results.get(i).getTitle());
        }
    }

    private void handleSelection(List<SearchResult> results) {
        while (true) {
            System.out.print("\nНомер статьи для открытия (Enter — новый поиск): ");

            if (!scanner.hasNextLine()) {
                return;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return;
            }

            int index = parseIndex(line, results.size());
            if (index < 0) {
                System.out.println(
                        "Неверный номер. Введите число от 1 до " + results.size());
                continue;
            }

            SearchResult selected = results.get(index);

            if (BrowserLauncher.open(selected.getPageId())) {
                System.out.println("Открываю статью: " + selected.getTitle());
            }

            return;
        }
    }

    private int parseIndex(String s, int size) {
        try {
            int n = Integer.parseInt(s);
            return (n >= 1 && n <= size) ? n - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}