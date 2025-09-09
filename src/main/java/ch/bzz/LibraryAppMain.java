package ch.bzz;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class LibraryAppMain {
    private static final Book BOOK_1 = new Book(1, "978-3-8362-9544-4", "Java ist auch eine Insel", "Christian Ullenboom", 2023);
    private static final Book BOOK_2 = new Book(2, "978-3-658-43573-8", "Grundkurs Java", "Dietmar Abts", 2024);
    
    // Database connection parameters - loaded from config.properties
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    
    static {
        loadConfig();
    }
    
    /**
     * Loads database configuration from config.properties file
     */
    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            DB_URL = props.getProperty("DB_URL");
            DB_USER = props.getProperty("DB_USER");
            DB_PASSWORD = props.getProperty("DB_PASSWORD");
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
            // Fallback to default values
            DB_URL = "jdbc:postgresql://localhost:5432/localdb";
            DB_USER = "localuser";
            DB_PASSWORD = "";
        }
    }
    
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
    
    /**
     * Reads books from a TSV file
     * @param filePath Path to the TSV file
     * @return List of Book objects from the file
     */
    public static List<Book> readBooksFromTsv(String filePath) {
        List<Book> books = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String isbn = parts[1].trim();
                        String title = parts[2].trim();
                        String author = parts[3].trim(); // authors column in TSV
                        int year = Integer.parseInt(parts[4].trim());
                        
                        books.add(new Book(id, isbn, title, author, year));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * Saves a list of books to the database (insert or update)
     * @param books List of books to save
     */
    public static void saveBooksToDatabase(List<Book> books) {
        String sql = "INSERT INTO books (id, isbn, title, author, publication_year) VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "isbn = EXCLUDED.isbn, " +
                    "title = EXCLUDED.title, " +
                    "author = EXCLUDED.author, " +
                    "publication_year = EXCLUDED.publication_year";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Book book : books) {
                stmt.setInt(1, book.getId());
                stmt.setString(2, book.getIsbn());
                stmt.setString(3, book.getTitle());
                stmt.setString(4, book.getAuthor());
                stmt.setInt(5, book.getYear());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            System.out.println("Successfully imported " + books.size() + " books.");
            
        } catch (SQLException e) {
            System.err.println("Database error while saving books: " + e.getMessage());
        }
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
                System.out.println("importBooks <FILE_PATH>");
            } else if (in.equals("listBooks")) {
                System.out.println(BOOK_1.getTitle());
                System.out.println(BOOK_2.getTitle());
            } else if (in.startsWith("importBooks ")) {
                // Extract file path from command
                String filePath = in.substring("importBooks ".length()).trim();
                if (!filePath.isEmpty()) {
                    List<Book> booksFromFile = readBooksFromTsv(filePath);
                    if (!booksFromFile.isEmpty()) {
                        saveBooksToDatabase(booksFromFile);
                    } else {
                        System.out.println("No books found in file or error reading file.");
                    }
                } else {
                    System.out.println("Please provide a file path. Usage: importBooks <FILE_PATH>");
                }
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
