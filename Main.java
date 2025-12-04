import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();

        try {
            // establish connection
            properties.load(new FileInputStream("app.properties"));
            String url = properties.getProperty("app.url");
            String username = properties.getProperty("app.username");
            String password = properties.getProperty("app.password");
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);

            conn.setAutoCommit(true); // default; turn off in the transaction workflow
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt("Choose an option: ");

                switch (choice) {
                    case 1 : viewProducts(conn);
                        break;
                    case 2 : viewCustomers(conn);
                        break;
                    case 3 : viewOrders(conn);
                        break;
                    case 4 : addCustomer(conn);
                        break;
                    case 5 : updateProductQuantity(conn);
                        break;
                    case 6 : deleteCustomer(conn);
                        break;
                    case 7 : runPlaceOrderTransaction(conn); // transactional workflow
                        break;
                    case 8 : runViewAndProcedureDemo(conn);  // view + stored procedure
                        break;
                    case 0 : running = false;
                        break;
                    default : System.out.println("Invalid option. Try again.");
                }
            }
            System.out.println("\nThank you for visiting the Farmer's Market Database");

            // close connection and scanner
            conn.close();
            scanner.close();
        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
    }

    private static void printMenu() {
        System.out.println("\n--- F4 Farmer's Market Console Menu ---");
        System.out.println("1. View Products");
        System.out.println("2. View Customers");
        System.out.println("3. View Orders");
        System.out.println("4. Insert New Customer");
        System.out.println("5. Update Product Quantity");
        System.out.println("6. Delete Customer");
        System.out.println("7. Place New Order");
        System.out.println("8. View and Change Order Status");
        System.out.println("0. Quit");
    }

    // helper to trim user input
    private static int readInt(String in) {
        while (true) {
            System.out.print(in);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    // helper to trim user input
    private static String readLine(String in) {
        System.out.print(in);
        return scanner.nextLine().trim();
    }
    //important when adding to customers
    public static String readNonEmptyLine(String in){
        while (true) {
            String line = readLine(in);
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This entry can't be empty.");
        }
    }
    // Select examples using prepared statements for tables (products, customers, orderr)
    private static void viewProducts(Connection conn) {
        String sql = "SELECT product_id, product_name, unit_price, quantity_available FROM Product";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nProducts:");
            while (rs.next()) {
                System.out.printf(
                        "%d: %s - $%.2f (qty: %d)%n",
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("unit_price"),
                        rs.getInt("quantity_available")
                );
            }
        } catch (SQLException e) {
            System.out.println("Issue with viewing products: " + e.getMessage());
        }
    }

    private static void viewCustomers(Connection conn) {
        String sql = "SELECT customer_id, first_name, last_name, email FROM Customer";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nCustomers:");
            while (rs.next()) {
                System.out.printf(
                        "%d: %s %s (%s)%n",
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) {
            System.out.println("Issue with viewing customers: " + e.getMessage());
        }
    }

    private static void viewOrders(Connection conn) {
        String sql = "SELECT order_id, customer_id, order_date, total_amount, order_status FROM Orderr";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nOrders:");
            while (rs.next()) {
                System.out.printf(
                        "Order ID: %d | Customer ID: %d | Order Date: %s | Total: %.2f | Status: %s %n",
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("order_status")
                );
            }
        } catch (SQLException e) {
            System.out.println("Issue with viewing orders: " + e.getMessage());
        }
    }

    // Insert ex to add a new customer
    private static void addCustomer(Connection conn) {
        String first = readNonEmptyLine("First name: ");
        String last = readNonEmptyLine("Last name: ");
        String email = readLine("Email: ");
        String phone = readLine("Phone: ");
        String address = readLine("Shipping address: ");

        String sql = """
            INSERT INTO Customer (first_name, last_name, email, phone, shipping_address, registration_date)
            VALUES (?, ?, ?, ?, ?, CURDATE())
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, address);

            int rows = ps.executeUpdate();
            System.out.println("Customer added. " + rows);
        } catch (SQLException e) {
            System.out.println("Issue with adding customer: " + e.getMessage());
        }
    }

    // Update ex by changing a products quantity
    private static void updateProductQuantity(Connection conn) {
        int productId = readInt("Enter product ID: ");
        int delta = readInt("Enter change in quantity (ex. -1 or 5): ");

        String sql = "UPDATE Product SET quantity_available = quantity_available + ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No product found with that ID.");
            } else {
                System.out.println("Quantity updated.");
            }
        } catch (SQLException e) {
            System.out.println("Issue with updating product quantity: " + e.getMessage());
        }
    }

    // Delete ex by deleting a customer completely
    private static void deleteCustomer(Connection conn) {
        int customerId = readInt("Enter customer ID to delete: ");

        String sql = "DELETE FROM Customer WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No customer found with that ID.");
            } else {
                System.out.println("Customer deleted.");
            }
        } catch (SQLException e) {
            System.out.println("Issue with deleting customer: " + e.getMessage());
        }
    }

    // ---- Step 4: Transactional workflow ----
    // Simple version: create an order with ONE product item.
    private static void runPlaceOrderTransaction(Connection conn) {
        int customerId = readInt("Customer ID: ");
        int productId = readInt("Product ID: ");
        int quantity = readInt("Quantity: ");

        String selectPrice = "SELECT unit_price FROM Product WHERE product_id = ?";
        String insertOrder = """
            INSERT INTO Orderr (customer_id, order_date, total_amount, order_status)
            VALUES (?, CURDATE(), ?, 'Order Placed')
            """;
        String insertOrderItem = """
            INSERT INTO OrderItem (order_id, product_id, quantity, subtotal_amount)
            VALUES (?, ?, ?, ?)
            """;

        try {
            conn.setAutoCommit(false);  // start transaction

            // 1. Get product price
            double unitPrice;
            try (PreparedStatement ps = conn.prepareStatement(selectPrice)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Invalid product ID, rolling back.");
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return;
                    }
                    unitPrice = rs.getDouble("unit_price");
                }
            }

            double subtotal = unitPrice * quantity;

            // 2. Insert into Order
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setDouble(2, subtotal);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to get generated order_id");
                    }
                    orderId = keys.getInt(1);
                }
            }

            // 3. Insert into OrderItem (this will also fire your trigger to reduce quantity)
            try (PreparedStatement ps = conn.prepareStatement(insertOrderItem)) {
                ps.setInt(1, orderId);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);
                ps.setDouble(4, subtotal);
                ps.executeUpdate();
            }

            // If we reach here, everything worked -> COMMIT
            conn.commit();
            System.out.println("Order placed successfully with order_id = " + orderId);

        } catch (SQLException e) {
            System.out.println("Error during order transaction, rolling back: " + e.getMessage());
            try {
                conn.rollback();  // ROLLBACK on failure
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true); // restore
            } catch (SQLException e) {
                System.out.println("Could not reset auto-commit: " + e.getMessage());
            }
        }
    }

    // Customer Order Summary
    private static void runViewAndProcedureDemo(Connection conn) {
        System.out.println("\nView: CustomerOrderSummary");
        String selectView = "SELECT * FROM CustomerOrderSummary";
        try (PreparedStatement ps = conn.prepareStatement(selectView);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf(
                        "Customer ID: %d | Name: %s %s | Order ID: %d | Ordered on: %s | Total: $%.2f (%s)%n",
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("order_id"),
                        rs.getDate("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("order_status")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error reading view: " + e.getMessage());
        }

        int orderId = readInt("\nEnter the Order ID for the order whose status needs to be changed (stored procedure): ");

        int orderStatus = readInt("\nSelect an option: \n1. Change order status to 'Order Shipped' \n2. Change order status to 'Delivered' \nEnter option: ");

        if (orderStatus == 1) {
            String callProc = "{CALL mark_order_shipped(?) }";
            try (CallableStatement cs = conn.prepareCall(callProc)) {
                cs.setInt(1, orderId);
                cs.execute();
                System.out.println("Stored procedure called. Check order status.");
            } catch (SQLException e) {
                System.out.println("Error calling stored procedure: " + e.getMessage());
            }
        }
        else if (orderStatus == 2) {
            String callProc = "{CALL mark_order_delivered(?) }";
            try (CallableStatement cs = conn.prepareCall(callProc)) {
                cs.setInt(1, orderId);
                cs.execute();
                System.out.println("Stored procedure called. Check order status.");
            } catch (SQLException e) {
                System.out.println("Error calling stored procedure: " + e.getMessage());
            }
        }
        else {
            System.out.println("Not a valid option.");
        }
        
    }
}
