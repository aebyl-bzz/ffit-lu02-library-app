package ch.bzz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LibraryAppMain {
    private static final Book BOOK_1 = new Book(1, "978-3-8362-9544-4", "Java ist auch eine Insel", "Christian Ullenboom", 2023);
    private static final Book BOOK_2 = new Book(2, "978-3-658-43573-8", "Grundkurs Java", "Dietmar Abts", 2024);
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/localdb";
    private static final String DB_USER = "localuser";
    private static final String DB_PASSWORD = "";
    
    /**
     * Connects to the database and retrieves all books
     * @return List of Book objects from the database
     */
    public static List<Book> getBooksFromDatabase() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, isbn, title, author, publication_year FROM books";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String author = rs.getString("author");
                int year = rs.getInt("publication_year");
                
                books.add(new Book(id, isbn, title, author, year));
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        
        return books;
    }
    
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
                System.out.println("importBooks");
            } else if (in.equals("listBooks")) {
                System.out.println(BOOK_1.getTitle());
                System.out.println(BOOK_2.getTitle());
            } else if (in.equals("importBooks")){
                List<Book> booksFromDb = getBooksFromDatabase();
                for (Book book : booksFromDb) {
                    System.out.println(book.getTitle());
                }
            } else {
                System.out.println(in);
            }
        }
        sc.close();
    }
}
