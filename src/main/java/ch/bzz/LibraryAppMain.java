package ch.bzz;

import java.util.Scanner;

public class LibraryAppMain {
    private static final Book BOOK_1 = new Book(1, "978-3-8362-9544-4", "Java ist auch eine Insel", "Christian Ullenboom", 2023);
    private static final Book BOOK_2 = new Book(2, "978-3-658-43573-8", "Grundkurs Java", "Dietmar Abts", 2024);
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean run = true;
        while (run) {
            String in = sc.nextLine();
            if (in.equals("quit")) {
                run = false;
            } else if (in.equals("help")) {
                System.out.println("help");
                System.out.println("quit");
                System.out.println("listBooks");
            } else if (in.equals("listBooks")) {
                System.out.println(BOOK_1.getTitle());
                System.out.println(BOOK_2.getTitle());
            } else {
                System.out.println(in);
            }
        }
        sc.close();
    }
}
