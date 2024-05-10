import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.time.temporal.ChronoUnit;

class Book implements Serializable {
    private String title;
    private String author;
    private String isbn;
    private boolean isAvailable;
    private LocalDate dueDate;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isAvailable = true;
        this.dueDate = null;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void toggleAvailability() {
        isAvailable = !isAvailable;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}

class Member implements Serializable {
    private String name;
    private String email;
    private String id;
    private List<Book> borrowedBooks;

    public Member(String name, String email, String id) {
        this.name = name;
        if (isValidEmail(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.id = id;
        this.borrowedBooks = new ArrayList<>();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrowBook(Book book) {
        borrowedBooks.add(book);
        book.toggleAvailability();
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
        book.toggleAvailability();
    }
}

class Library implements Serializable {
    private List<Book> books;
    private List<Member> members;
    private transient ScheduledExecutorService executor; // transient to exclude from serialization

    public Library() {
        this.books = new ArrayList<>();
        this.members = new ArrayList<>();
        this.executor = Executors.newScheduledThreadPool(2);
        loadLibraryData();
        scheduleBackgroundTasks();
        initializeBooks();
    }

    private void initializeBooks() {
        addBook("Book of Lies", "Maake Katlego", "sbn1");
        addBook("JavaScript", "Sello Sbu", "sbn2");
        // Add more books as needed
    }

    private void scheduleBackgroundTasks() {
        executor.scheduleAtFixedRate(() -> {
            updateFines();
            sendNotifications();
        }, 0, 24, TimeUnit.HOURS);
    }

    public void addBook(String title, String author, String isbn) {
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        System.out.println("Book '" + title + "' added successfully.");
    }

    public void addMember(String name, String email, String id) {
        Member newMember = new Member(name, email, id);
        members.add(newMember);
        System.out.println("Member '" + name + "' added successfully.");
    }

    public List<Book> searchBooksByTitle(String title) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> searchBooksByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                result.add(book);
            }
        }
        return result;
    }

    public Member findMemberByEmail(String email) {
        for (Member member : members) {
            if (member.getEmail().equalsIgnoreCase(email)) {
                return member;
            }
        }
        return null;
    }

    public void checkoutBook(Member member, Book book) throws Exception {
        if (book.isAvailable()) {
            book.setDueDate(LocalDate.now().plusDays(14)); // Due in 14 days
            member.borrowBook(book);
        } else {
            throw new Exception("Book is not available for checkout");
        }
    }

    public void returnBook(Member member, Book book) {
        member.returnBook(book);
        book.setDueDate(null); // Reset due date
    }

    void updateFines() {
        LocalDate today = LocalDate.now();
        for (Member member : members) {
            for (Book book : member.getBorrowedBooks()) {
                if (book.getDueDate() != null && today.isAfter(book.getDueDate())) {
                    long daysOverdue = ChronoUnit.DAYS.between(book.getDueDate(), today);
                    double fine = daysOverdue * 1.0; // $1 per day overdue
                    System.out.println("Fine for " + member.getName() + ": $" + fine);
                }
            }
        }
    }

    private void sendNotifications() {
        LocalDate today = LocalDate.now();
        for (Member member : members) {
            for (Book book : member.getBorrowedBooks()) {
                if (book.getDueDate() != null && today.plusDays(1).equals(book.getDueDate())) {
                    System.out.println("Notification: Your book '" + book.getTitle() + "' is due tomorrow.");
                }
                if (book.getDueDate() != null && today.isAfter(book.getDueDate())) {
                    System.out.println("Notification: Your book '" + book.getTitle() + "' is overdue.");
                }
            }
        }
    }

    public void saveLibraryData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("library_data.ser"))) {
            oos.writeObject(this);
            System.out.println("Library data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLibraryData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("library_data.ser"))) {
            Library library = (Library) ois.readObject();
            this.books = library.books;
            this.members = library.members;
            System.out.println("Library data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous library data found. Starting fresh.");
        }
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void borrowBook(String email, String isbn) throws Exception {
        Member member = findMemberByEmail(email);
        if (member == null) {
            throw new Exception("Member not found with email: " + email);
        }
        Book bookToBorrow = null;
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                bookToBorrow = book;
                break;
            }
        }
        if (bookToBorrow == null) {
            throw new Exception("Book not found with ISBN: " + isbn);
        }
        checkoutBook(member, bookToBorrow);
    }
}

public class LibraryApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        try {
            while (true) {
                showMenu();
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        addMember();
                        break;
                    case 3:
                        searchBooks();
                        break;
                    case 4:
                        borrowBook();
                        break;
                    case 5:
                        returnBook();
                        break;
                    case 6:
                        viewDueDatesAndFines(); // Added option to view due dates and fines
                        break;
                    case 7:
                        exit();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } finally {
            library.shutdownExecutor();
        }
    }

    private static void showMenu() {
        System.out.println("\n--- Library Management System Menu ---");
        System.out.println("1. Add New Book");
        System.out.println("2. Add New Member");
        System.out.println("3. Search Books");
        System.out.println("4. Borrow Book");
        System.out.println("5. Return Book");
        System.out.println("6. View Due Dates And Fines"); // Updated menu option
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void addBook() {
        System.out.print("Enter the title of the book: ");
        String title = scanner.nextLine();
        System.out.print("Enter the author of the book: ");
        String author = scanner.nextLine();
        System.out.print("Enter the ISBN of the book: ");
        String isbn = scanner.nextLine();
        library.addBook(title, author, isbn);
    }

    private static void addMember() {
        System.out.print("Enter the full name of the member: ");
        String name = scanner.nextLine();
        System.out.print("Enter the email of the member: ");
        String email = scanner.nextLine();
        System.out.print("Enter the ID of the member: ");
        String id = scanner.nextLine();
        library.addMember(name, email, id);
    }

    private static void searchBooks() {
        System.out.println("Search books by:");
        System.out.println("1. Title");
        System.out.println("2. Author");
        System.out.print("Enter your choice: ");
        int searchChoice = scanner.nextInt();
        scanner.nextLine();
        switch (searchChoice) {
            case 1:
                System.out.print("Enter the title: ");
                String title = scanner.nextLine();
                List<Book> booksByTitle = library.searchBooksByTitle(title);
                displayBooks(booksByTitle);
                break;
            case 2:
                System.out.print("Enter the author: ");
                String author = scanner.nextLine();
                List<Book> booksByAuthor = library.searchBooksByAuthor(author);
                displayBooks(booksByAuthor);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void displayBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("Found books:");
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                System.out.println((i + 1) + ". " + book.getTitle() + " by " + book.getAuthor() + " (ISBN: " + book.getIsbn() + ")");
            }
        }
    }

    private static void borrowBook() {
        System.out.println("Available Books:");
        List<Book> allBooks = new ArrayList<>(library.getBooks());
        for (int i = 0; i < allBooks.size(); i++) {
            Book book = allBooks.get(i);
            System.out.println((i + 1) + ". " + book.getTitle() + " by " + book.getAuthor() + " (ISBN: " + book.getIsbn() + ")");
        }
        System.out.print("Enter the number of the book you want to borrow: ");
        int bookIndex = scanner.nextInt();
        scanner.nextLine();
        if (bookIndex < 1 || bookIndex > allBooks.size()) {
            System.out.println("Invalid book selection.");
            return;
        }
        Book selectedBook = allBooks.get(bookIndex - 1);
        try {
            System.out.print("Enter your email: ");
            String email = scanner.nextLine();
            System.out.print("Enter the ISBN of the book: ");
            String isbn = scanner.nextLine();
            library.borrowBook(email, isbn);
            System.out.println("Book successfully borrowed. The book is now unavailable.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBook() {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter the ISBN of the book to return: ");
        String isbn = scanner.nextLine();

        try {
            Member member = library.findMemberByEmail(email);
            if (member == null) {
                System.out.println("Member not found with email: " + email);
                return;
            }

            Book bookToReturn = null;
            for (Book book : member.getBorrowedBooks()) {
                if (book.getIsbn().equals(isbn)) {
                    bookToReturn = book;
                    break;
                }
            }

            if (bookToReturn == null) {
                System.out.println("Book not found with ISBN: " + isbn);
                return;
            }

            library.returnBook(member, bookToReturn);
            System.out.println("Book returned successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewDueDatesAndFines() {
        System.out.println("View due dates and fines:");
        // Process overdue fines
        library.updateFines();
        // Print due dates and fines
        boolean anyDueDates = false;
        for (Member member : library.getMembers()) {
            if (!member.getBorrowedBooks().isEmpty()) {
                System.out.println("Member: " + member.getName());
                System.out.println("Borrowed Book: " + member.getBorrowedBooks());
                // Calculate Due Date
                LocalDate dueDate = LocalDate.now().plusDays(14);
                System.out.println("Due Date: " + dueDate);
                // Calculate Overdue Fines
                LocalDate currentDate = LocalDate.now();
                if (currentDate.isAfter(dueDate)) {
                    long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
                    double fineAmount = daysOverdue * 1.0; // R1 per day overdue
                    System.out.println("Overdue Days: " + daysOverdue);
                    System.out.println("Fine Amount: R" + fineAmount);
                    anyDueDates = true;
                } else {
                    System.out.println("Status: Not Overdue");
                }
                System.out.println();
            }
        }
        if (!anyDueDates) {
            System.out.println("No overdue fines and dates to display.");
        }
    }

    private static void exit() {
        System.out.println("Saving library data...");
        library.saveLibraryData();
        System.out.println("Exiting program...");
        library.shutdownExecutor();
    }
}
