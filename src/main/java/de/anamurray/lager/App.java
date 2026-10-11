package de.anamurray.lager;

import de.anamurray.lager.repository.CsvArticleRepository;
import de.anamurray.lager.service.InventoryService;
import de.anamurray.lager.ui.ConsoleMenu;

import java.nio.file.Path;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Path dataFile = Path.of(args.length > 0 ? args[0] : "data/artikel.csv");
        InventoryService service = new InventoryService(new CsvArticleRepository(dataFile));
        new ConsoleMenu(service, new Scanner(System.in)).run();
    }
}
