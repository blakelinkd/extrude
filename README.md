# SQL Viewer Application

The SQL Viewer application is a Java-based tool that allows users to view and interact with SQL database tables and views. It provides a graphical user interface (GUI) for browsing and querying data in an SQLite database.

## Features

- **Table View**: Displays the contents of the selected table or view in a tabular format. The data is fetched from the connected SQLite database.
- **Pagination**: Supports pagination for large tables or views. Users can navigate through the data using the "Next" and "Prev" buttons.
- **View Selection**: Allows users to select a specific view from the available views in the database. Double-clicking on a view in the view list will display its contents in the table view.
- **Database Schema**: Provides a button to view the database schema, including table names, column names, data types, and sizes.
- **Syntax Highlighting**: Uses the RSyntaxTextArea library to display the SQL schema in a syntax-highlighted text area.

## Getting Started

To run the SQL Viewer application, follow these steps:

1. Ensure that you have Java Development Kit (JDK) installed on your system.
2. Clone the repository to your local machine.
3. Open the project in your preferred Java Integrated Development Environment (IDE).
4. Set up the necessary dependencies:
   - The application uses the following external libraries:
     - `javax.swing`: Provides the GUI components.
     - `javax.swing.table`: Handles the table-related functionality.
     - `java.awt`: Deals with GUI-related classes and methods.
     - `java.net.URL`: Represents a Uniform Resource Locator (URL).
     - `java.sql`: Allows Java programs to connect to and interact with databases.
     - `org.fife.ui.rsyntaxtextarea`: Offers syntax highlighting for the SQL schema.
   - Ensure that these libraries are properly imported into your project.
5. Build and run the `App` class, which contains the main method.

## Usage

Upon launching the SQL Viewer application, you will see the main window with the following components:

- **Original Table Tab**: Displays the contents of the selected table or view in a tabular format.
- **View Selection Tab**: Lists the available views in the connected database. Double-clicking on a view will display its contents in the table view.
- **Navigation Buttons**: Allows you to navigate through the data when pagination is enabled.
- **View Schema Button**: Opens a new window displaying the database schema.

To interact with the application, follow these steps:

1. Connect to an SQLite database by modifying the `DB_URL` constant in the `App` class to point to the desired database file.
2. Run the application, and the main window will appear.
3. Select a view from the "Select View" tab by double-clicking on the desired view in the view list.
4. The contents of the selected view will be displayed in the "Original Table" tab.
5. If the view contains a large number of rows, use the navigation buttons to paginate through the data.
6. To view the database schema, click the "View Schema" button. A new window will open, displaying the schema information.

## Building

The SQL Viewer application relies on the following external libraries:

- `javax.swing` (included in the Java Development Kit)
- `javax.swing.table` (included in the Java Development Kit)
- `java.awt` (included in the Java Development Kit)
- `java.net.URL` (included in the Java Development Kit)
- `java.sql` (included in the Java Development Kit)
- `org.fife.ui.rsyntaxtextarea` (external library for syntax highlighting)

Please ensure that these dependencies are properly set up in your project before running the application.
To build the Maven project with the provided `pom.xml` file, follow these instructions:

1. Make sure you have Maven installed on your system. You can verify this by running the following command in your terminal or command prompt:
   ```
   mvn -version
   ```
   If Maven is not installed, you can download it from the [Apache Maven website](https://maven.apache.org/download.cgi) and follow the installation instructions.

2. Navigate to the directory containing the `pom.xml` file in your terminal or command prompt.

3. Run the following command to build the project and create an executable JAR file:
   ```
   mvn clean package
   ```
   This command will compile the source code, run tests (if any), and package the project into a JAR file with dependencies.

4. After the build is successful, you will find the executable JAR file named `extrude-1.0-SNAPSHOT-jar-with-dependencies.jar` in the `target` directory.

5. You can run the application using the following command:
   ```
   java -jar target/extrude-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
   This will launch the SQL Viewer application.

The Maven build process will handle the dependency resolution and create an executable JAR file using the Maven Assembly Plugin. The `maven-assembly-plugin` configuration in the `pom.xml` file ensures that the JAR file includes all the necessary dependencies.

Note: The `maven-assembly-plugin` creates an executable JAR file with dependencies included, which means it will be larger in size compared to a regular JAR file.
