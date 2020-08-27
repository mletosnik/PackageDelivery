package com.letosnik;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PackageDelivery {

    // interval of summary printing in seconds
    static final int PRINT_INTERVAL = 60;
    // regEx to validate user input
    static final String inputRegEx = "^\\d*\\.?\\d{0,3}\\s\\d{5}$";
    HashMap<String, Double> postPackages = new HashMap<>();

    /**
     * Validates provided string (user input) with a regEx, parses entered data and saves them to hash map
     * with postal code as a key. If the hash map already contains such postal code, its value is increased by weight.
     *
     * @param input User input
     */
    private void tryInsertData(String input) {
        if (input.matches(inputRegEx)) {
            String[] parts = input.split("\\s");
            double weight = Double.parseDouble(parts[0]);
            String postalCode = parts[1];

            postPackages.put(postalCode, postPackages.getOrDefault(postalCode, (double) 0) + weight);
        } else {
            System.err.println("Wrong input. Please use this format:");
            System.err.println("<weight: positive number, >0, max 3 decimal places, dot as decimal separator><space><postal code: fixed 5 digits>");
        }
    }

    /**
     * Waits for a user input until "quit" is entered.
     */
    private void processInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String input = null;
        boolean quit = false;
        while (!quit) {
            try {
                input = reader.readLine();
            } catch (IOException e) {
                System.err.println("Input error");
                System.err.println(e.toString());
            }

            if (input != null) {
                quit = input.compareToIgnoreCase("quit") == 0;

                if (!quit) {
                    tryInsertData(input);
                }
            }
        }
    }

    /**
     * Checks whether a filename was entered as a command line argument and if so, the file is loaded the same way as user input.
     *
     * @param args Command line arguments (only first argument is checked for a file name)
     */
    private void readInitialLoad(String[] args) {
        if (args.length > 0) {
            String fileName = args[0];
            File file = new File(fileName);

            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + fileName);
            }

            String line;
            if (bufferedReader != null) {
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        // read the line as a normal user input
                        tryInsertData(line);
                    }
                } catch (IOException e) {
                    System.err.println("Error while reading file: " + fileName);
                }
            }

        }
    }

    /**
     * Prints summary of packages ordered by total weight.
     */
    private void printPostPackages() {
        // to be sure that decimal separator is dot
        Locale locale  = new Locale("en", "UK");
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        // 3 fixed decimal places
        df.applyPattern("#.000");

        System.out.println("Current state (postal code, total weight):");
        postPackages.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> System.out.println(entry.getKey() + " " + df.format(entry.getValue())));
    }

    /**
     * Runs scheduled task that prints summary in the given interval, then reads possible initial load file and finally
     * waits for a user input.
     *
     * @param args Command line arguments
     */
    private void run(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        final Runnable listCurrent = this::printPostPackages;
        scheduledExecutorService.scheduleAtFixedRate(listCurrent, PRINT_INTERVAL, PRINT_INTERVAL, TimeUnit.SECONDS);

        readInitialLoad(args);
        processInput();

        scheduledExecutorService.shutdown();
    }

    /**
     * Application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        PackageDelivery packageDelivery = new PackageDelivery();
        packageDelivery.run(args);
    }

}
