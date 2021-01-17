package com.kennysettens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class FileIO {
    private FileReader fileReader;
    private BufferedReader txtBufferReader;
    private List<String> filePaths;
    private final Scanner keyboard;
    private String userResponse;//should this variable be something dynamic?

    FileIO() {
        filePaths = new ArrayList<>();
        userResponse = "";
        keyboard = new Scanner(System.in);
        start();
    }

    private void askIfUserWantsTutorial(){
        System.out.println("want tutorial? y/n");
        userResponse = getUserResponse();
        switch (userResponse){
            case "n":
                return;
            default:
                giveUserTutorial();
        }
    }

    private void giveUserTutorial(){
        System.out.println("This program indexes either 1 text file or directory containing text files\n " +
                "if you enter a directory you will be asked if you want any files in that directory" +
                " not to be indexed make sure you enter the file as shown for example\n" +
                "/home/user/problems/text2.txt\n" +
                "Cannot be removed by text2.txt\n" +
                " It is removed by typing /home/user/problems/text2.txt\n");

        System.out.println("when indexing is done you will see what line your search" +
                " query appears on.\n" +
                " You can then enter another file or directory\n");
    }
    
    private String getUserResponse(){
        return keyboard.nextLine();
    }

    private void start(){
        askIfUserWantsTutorial();

        while (!userResponse.equalsIgnoreCase("q")){
            handleEnteredFile(getFileToIndex());
        }
    }

    private void initializeFileList(String filePath){
        File directory = new File(filePath);

        for (File file : Objects.requireNonNull(directory.listFiles())){
            if (isTextFile(file.getAbsolutePath()))
                filePaths.add(file.getPath());
        }
    }
    
    private File getFileToIndex(){
        System.out.println("Enter a full or relative path of a text file or directory you want indexed (q to quit)");
        userResponse = getUserResponse();
        didUserQuit();

        return new File(userResponse);
    }

    private void didUserQuit(){
        //void instead of boolean because whole program closes
        if (userResponse.equals("q"))
            System.exit(0);
    }

    private void handleEnteredFile(File file){
        if (!file.canRead()){
            System.out.println("cannot read file or directory");
            handleEnteredFile(getFileToIndex());
        }

        if (file.isDirectory()) {
            if (canIndexDirectory(file)){
                initializeFileList(file.getPath());
                askWhatFilesNotToQuery();
                searchTxtFileForWord(filePaths, getSearchQuery());
            }
            else {
                System.out.println("entered directory has no text files that can be indexed");
                handleEnteredFile(getFileToIndex());
            }
        }
        else if (!isTextFile(file.getPath())){
            System.out.println("not a text file");
            handleEnteredFile(getFileToIndex());
        }
        else if (!file.exists()) {
            System.out.println("file doesn't exist");
            handleEnteredFile(getFileToIndex());
        }
        else if (file.isFile())
            searchTxtFileForWord(new File(file.getPath()), getSearchQuery());
        else
            System.out.println("something went wrong");
            handleEnteredFile(getFileToIndex());
    }

    private boolean canIndexDirectory(File directory){
        for (File file : Objects.requireNonNull(directory.listFiles())){
            if (isTextFile(file.getPath()) && file.canRead())
                return true;
        }
        return false;
    }

    private void askWhatFilesNotToQuery(){
        while (!userResponse.equalsIgnoreCase("done")) {
            System.out.println("Enter the file you want removed from the list, (type done to be done)");
            listTxtFiles();
            userResponse = getUserResponse();
            removeFile(userResponse);
        }
    }

    private void removeFile(String userResponse){
        filePaths.removeIf(filePath -> filePath.equals(userResponse));
    }

    private String getSearchQuery(){
        System.out.println("What word would you like to search for in the documents");
        didUserQuit();
        return getUserResponse();
    }

    private void listTxtFiles(){
        for (String file : filePaths)
            if (isTextFile(file)) {
                System.out.print(file + " ");
            }
        System.out.println();
    }

    private boolean isTextFile(String file){
        if (file == null)
            return false;

        return file.endsWith(".txt");
    }

    private void searchTxtFileForWord(List<String> filePaths, String searchQuery){

        for (String filePath : filePaths){
            searchTxtFileForWord(new File(filePath), searchQuery);
        }
    }

    private void searchTxtFileForWord(File file, String searchQuery) {
        //doesn't return string since i want the search results on console
        boolean wordGotFound = false;
        String line = "";
        int[] counter = {1};
        initializeFileReader(file);
        try {
            while ((line = txtBufferReader.readLine()) != null){
                if (containsWord(line, searchQuery)) {
                    System.out.println("found on line " + counter[0] + " of file " + file.getPath());
                    wordGotFound = true;
                }
                ++counter[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!wordGotFound)
            System.out.println("word was not found in " + file + "\n");
        closeFileReader();
    }

    private void initializeFileReader(File file) {
        try {
            fileReader = new FileReader(file);
            txtBufferReader = new BufferedReader(fileReader);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void closeFileReader() {
        try {
        fileReader.close();
        txtBufferReader.close();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static boolean containsWord(String line, String query) {
        int index = line.indexOf(query);
        if (index == -1) {
            return false;
        }
        if (index > 0 && line.charAt(index - 1) != ' ') {
            return false;
        }
        return index + query.length() >= line.length() ||
                line.charAt(index + query.length()) == ' ';
    }
}
