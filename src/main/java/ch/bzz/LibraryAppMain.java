package ch.bzz;

import java.util.Scanner;

public class LibraryAppMain {
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
            } else {
                System.out.println(in);
            }
        }
        sc.close();
    }
}
