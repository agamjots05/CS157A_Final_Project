-- create tables
CREATE TABLE Farmer (
                        farmer_id INT AUTO_INCREMENT PRIMARY KEY,
                        farm_name VARCHAR(255) NOT NULL,
                        contact_person VARCHAR(255),
                        email VARCHAR(255) UNIQUE,
                        phone VARCHAR(50),
                        address VARCHAR(255)
);

CREATE TABLE Customer (
                          customer_id INT AUTO_INCREMENT PRIMARY KEY,
                          first_name VARCHAR(255) NOT NULL,
                          last_name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE,
                          phone VARCHAR(50),
                          shipping_address VARCHAR(255),
                          registration_date DATE
);

CREATE TABLE Category (
                          category_name VARCHAR(255) PRIMARY KEY,
                          description TEXT
);

CREATE TABLE Product (
                         product_id INT AUTO_INCREMENT PRIMARY KEY,
                         product_name VARCHAR(255) NOT NULL,
                         description TEXT,
                         unit_of_measure VARCHAR(50),
                         unit_price DECIMAL(10, 2) NOT NULL,
                         quantity_available INT NOT NULL,
                         farmer_id INT,
                         category_name VARCHAR(255),
                         FOREIGN KEY (farmer_id) REFERENCES Farmer(farmer_id)
                             ON DELETE CASCADE
                             ON UPDATE CASCADE,
                         FOREIGN KEY (category_name) REFERENCES Category(category_name)
                             ON DELETE SET NULL
                             ON UPDATE CASCADE
);

CREATE TABLE `Order` (
                         order_id INT AUTO_INCREMENT PRIMARY KEY,
                         customer_id INT,
                         order_date DATE NOT NULL,
                         total_amount DECIMAL(10, 2) NOT NULL,
                         order_status VARCHAR(50) CHECK (order_status IN ("Order Placed", "Order Shipped", "Delivered")),
                         FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
                             ON DELETE CASCADE
                             ON UPDATE CASCADE
);

CREATE TABLE OrderItem (
                           order_id INT,
                           product_id INT,
                           quantity INT NOT NULL,
                           subtotal_amount DECIMAL(10, 2) NOT NULL,
                           FOREIGN KEY (order_id) REFERENCES `Order`(order_id)
                               ON DELETE CASCADE
                               ON UPDATE CASCADE,
                           FOREIGN KEY (product_id) REFERENCES Product(product_id)
                               ON DELETE CASCADE
                               ON UPDATE CASCADE,
                           PRIMARY KEY (order_id, product_id)
);



-- insert data
INSERT INTO Farmer (farm_name, contact_person, email, phone, address)
VALUES ("Green Valley Acres", "Samuel Turner", "sam.turner@greenvalleyfarms.com", "(408)555-1234", "124 Meadowbrook Rd, Helena, MT 59601"),
       ("Sunrise Harvest Farm", "Linda Cooper", "linda.cooper@sunriseharvest.com", "(309) 555-9472", "89 Harvest Ln, Peoria, IL 61615"),
       ("Oakridge Dairy", "Martin Alvarez", "martin.alvarez@oakridgedairy.com", "(207) 555-3376", "452 Maple Rd, Bangor, ME 04401"),
       ("Willow Creek Ranch", "Natalie Brooks", "n.brooks@willowcreekranch.com", "(575) 555-8019", "230 County Hwy 7, Roswell, NM 88201"),
       ("Prairie Blossom Farms", "Jacob Nguyen", "jacob.nguyen@prairieblossom.com", "(701) 555-4790", "62 Prairie Dr, Fargo, ND 58104");

INSERT INTO Customer (first_name, last_name, email, phone, shipping_address, registration_date)
VALUES ("Hannah", "Mitchell", "hmitchell@outlook.com", "(219) 555-4712", "77 Oakwood Dr, Gary, IN 46403", "2023-08-17"),
       ("Derek", "Lawson", "d.lawson@gmail.com", "(402) 555-8621", "215 Birch St, Omaha, NE 68104", "2024-02-03"),
       ("Sophia", "Nguyen", "sophia.nguyen@gmail.com", "(512) 555-9018", "215 Birch St, Omaha, NE 68104", "2024-11-10"),
       ("Marcus", "Patel", "marcus.patel@yahoo.com", "(406) 555-2749", "348 Elm Ct, Billings, MT 59101", "2025-03-26"),
       ("Olivia", "Torres", "oliviatorres@@gmail.com", "(610) 555-7380", "128 Spring Rd, Allentown, PA 18103", "2025-09-14");

INSERT INTO Category
VALUES ("Dairy", "Food products made from milk"),
       ("Produce", "Fruits or vegetables"),
       ("Meat", "Beef, chicken, pork, or fish"),
       ("Nuts/Seeds", "Product contains either nuts or seeds"),
       ("Honey", "Locally sourced honey"),
       ("Eggs", "Eggs from chicken, geese, or other poultry");

INSERT INTO Product (product_name, description, unit_of_measure, unit_price, quantity_available, farmer_id, category_name)
VALUES ("Butter", "Freshly churned butter from raw milk", "8 oz", 5.99, 5, 3, "Dairy"),
       ("Heirloom Tomatoes", "Large, multi-colored mix of tomatoes", "1 lb", 3.00, 10, 2, "Produce"),
       ("Granny Smith Apples", "Green, tangy apples perfect for baking", "1 lb", 2.50, 50, 4, "Produce"),
       ("Large Brown Eggs", "Grade A, Pasture-Raised Chicken Eggs", "1 dozen", 10.99, 10, 1, "Eggs"),
       ("Romaine Lettuce", "Organic Romaine lettuce", "1 count", 2.99, 10, 2, "Produce"),
       ("Walnuts", "Organic raw walnuts", "16 oz", 7.50, 10, 4, "Nuts/Seeds"),
       ("Wildflower Honey", "100% pure, raw wildflower honey", "12 oz", 6.50, 20, 5, "Honey"),
       ("Whole Chicken", "Raw, whole chicken. Cage free and no antibiotics!", "4 lb", 5.98, 12, 1, "Meat");

INSERT INTO `Order` (customer_id, order_date, total_amount, order_status)
VALUES (1, "2025-11-1", 5.99, "Order Shipped"),
       (2, "2025-11-2", 11.99, "Order Placed"),
       (5, "2025-10-25", 10.98, "Delivered"),
       (4, "2025-10-29", 13.00, "Order Shipped"),
       (3, "2025-11-1", 10.99, "Order Placed");

INSERT INTO OrderItem
VALUES (2,1,1, 5.99),
       (2, 2, 3, 9.00),
       (2, 5, 1, 2.99),
       (3, 8, 1, 5.98),
       (4, 7, 2, 13.00),
       (5, 4, 1, 10.99),
       (3, 3, 2, 5.00);

--view
CREATE OR REPLACE VIEW CustomerOrderSummary AS
SELECT
    C.customer_id,
    C.first_name,
    C.last_name,
    O.order_id,
    O.order_date,
    O.total_amount,
    O.order_status
FROM Customer C
         JOIN Orderr O ON C.customer_id = O.customer_id;

-- procedure
DELIMITER //
DROP PROCEDURE IF EXISTS mark_order_shipped //
CREATE PROCEDURE mark_order_shipped(IN p_order_id INT)
BEGIN
UPDATE Orderr
SET order_status = 'Order Shipped'
WHERE order_id = p_order_id
  AND order_status = 'Order Placed';
END //
DELIMITER ;

-- procedure 2
DELIMITER //
DROP PROCEDURE IF EXISTS mark_order_delivered //
CREATE PROCEDURE mark_order_delivered(IN p_order_id INT)
BEGIN
UPDATE Orderr
SET order_status = 'Delivered'
WHERE order_id = p_order_id
  AND order_status = 'Order Shipped';
END //
DELIMITER ;



-- trigger
DELIMITER //
CREATE TRIGGER update_product_quantity_after_order
    AFTER INSERT ON OrderItem
    FOR EACH ROW
BEGIN
    UPDATE Product
    SET quantity_available = quantity_available - NEW.quantity
    WHERE product_id = NEW.product_id;
END //
DELIMITER ;


--index
CREATE INDEX idx_product_category
    ON Product (category_name);
