package org.example;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ArrayList<ModLoaderThread> remainingThreads = new ArrayList<>();
        Scanner userInput = new Scanner(System.in);
        String path = System.getProperty("user.home")+"\\AppData\\Roaming\\.minecraft\\mods";
        boolean loading = false;
        do {
            if(remainingThreads.isEmpty()){
                if(loading){
                    System.out.println("\nLoad Was Successful!");
                }
                System.out.println();
                System.out.println("0) Exit");
                ArrayList<File> foldersOptions = displayMenu(path);
                System.out.println("Current Mods Loaded: "+getCurrentFolder(path));
                System.out.print("\nChoose an option: ");
                String option = userInput.nextLine().strip();
                if(option.equalsIgnoreCase("0")){
                    System.exit(0);
                }
                try {
                    System.out.println("\nLoading Mods...");
                    loading = true;
                    loadMods(path,foldersOptions.get(Integer.parseInt(option)-1),remainingThreads);
                }catch (NumberFormatException | IndexOutOfBoundsException e){
                    System.out.println(e);
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }while (true);
    }
    public static void loadMods(String path, File folderToLoad, ArrayList<ModLoaderThread> remainingThreads){
        emptyModsFolder(path);
        for(File f : folderToLoad.listFiles()){
            if(f.getName().split("\\.")[f.getName().split("\\.").length-1].equalsIgnoreCase("jar")){
                ModLoaderThread thread = new ModLoaderThread(path, f);
                remainingThreads.add(thread);
                thread.addRemainingThreads(remainingThreads);
                thread.start();
            }
        }
        saveCurrentFolder(path, folderToLoad.getName());
    }
    public static void saveCurrentFolder(String path, String folderName){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path+"/.swapData.txt"));
            writer.write(folderName);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static String getCurrentFolder(String path){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path+"/.swapData.txt"));
            String s = reader.readLine();
            if(s==null || s.isBlank()){
                return "[Unknown]";
            }
            return s;
        } catch (IOException e) {
            return "[Not Found]";
        }
    }
    public static void emptyModsFolder(String path){
        File modsDir = new File(path);
        for(File f : modsDir.listFiles()){
            if(f.isFile() && f.getName().split("\\.")[f.getName().split("\\.").length-1].equalsIgnoreCase("jar")){
                f.delete();
            }
        }
    }
    public static ArrayList<File> displayMenu(String path){
        int counter = 1;
        ArrayList<File> folders = new ArrayList<>();
        for(File f : new File(path).listFiles()){
            if(f.isDirectory()){
                folders.add(f);
                System.out.println(counter+") "+f.getName());
                counter++;
            }
        }
        return folders;
    }
    static class ModLoaderThread extends Thread {
        private String path;
        private File modToLoad;
        private ArrayList<ModLoaderThread> remainingThreads;
        ModLoaderThread(String path, File modToLoad){
            this.path = path;
            this.modToLoad = modToLoad;
        }
        private void addRemainingThreads(ArrayList<ModLoaderThread> remainingThreads){
            this.remainingThreads = remainingThreads;
        }
        @Override
        public void run() {
            loadThreadMod(path, modToLoad);
            synchronized (remainingThreads){
                remainingThreads.remove(this);
            }
        }
        private void loadThreadMod(String path, File modToLoad){
            try {
                byte[] b = Files.readAllBytes(modToLoad.toPath());
                Files.write(new File(path+"/"+modToLoad.getName()).toPath(), b);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
