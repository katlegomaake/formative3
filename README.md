Library Management System - README
Introduction
This project is an upgraded version of a Library Management System. It introduces new functionalities such as tracking due dates for borrowed books, calculating overdue fines, background processing for notifications and fine updates, and data persistence using file streams. The system is designed to handle these features efficiently while ensuring a seamless user experience.

Project Structure
The project consists of several classes:

Book: Represents a book in the library with attributes like title, author, ISBN, availability, and due date.
Member: Represents a member of the library with attributes like name, email, ID, and a list of borrowed books.
Library: Manages the collection of books, members, and background tasks like fine calculation and notification sending.
LibraryApp: Contains the main method and the text-based user interface for interacting with the library functionalities.
New Features
1. Due Dates and Overdue Fines
Due Date Tracking: When a book is borrowed, the system sets a due date based on the current date plus a predetermined loan period.
Overdue Fine Calculation: The system calculates fines for each day a book is overdue. Fines increase with each day overdue.
2. Background Notifications and Fine Processing
Background Tasks: Utilizes multi-threading to perform background tasks such as sending reminders for due books and processing overdue fines in parallel with the main application flow.
Notification Sending: Another thread sends out notifications to members with due or overdue books.
3. Persistence Using File Streams
Data Persistence: Saves library data, including books, members, and transaction logs, to files using streams. Data is loaded from files when the application starts.
Instructions for Use
Compile: Compile the Java files using a Java compiler.
Run: Run the LibraryApp class to start the application.
Menu Navigation: Use the menu options to perform various actions such as adding books/members, searching books, borrowing/returning books, and managing notifications.
View Due Dates and Fines: Select the "Manage Notifications" option from the menu to view due dates and fines for borrowed books.
Save Data: The library data is automatically saved to a file (library_data.ser) when the program exits.
Error Handling
Proper error handling is implemented for file operations, including scenarios where data files are missing, corrupted, or inaccessible.
Concurrency issues are addressed to ensure data integrity when multiple threads access and modify shared resources.
Future Enhancements
Implement a graphical user interface (GUI) for a more interactive user experience.
Integrate with an external email service for sending notifications to members.
Provide options for customizing loan periods and fine calculation rates.
Conclusion
This upgraded Library Management System offers enhanced functionality and reliability. It provides an efficient way to manage library operations while ensuring data persistence and integrity.

