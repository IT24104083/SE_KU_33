-- 1. FIRST CLEAR ALL DATA (in reverse dependency order)
DELETE FROM Complaint;
DELETE FROM Feedback;
DELETE FROM Invoice;
DELETE FROM VendorAvailability;
DELETE FROM VenueAvailability; 
DELETE FROM EventVendor;
DELETE FROM Event;
DELETE FROM EventInquiry;
DELETE FROM Vendor;
DELETE FROM Venue;
DELETE FROM Customer;

-- 2. RESET IDENTITY COUNTERS
DBCC CHECKIDENT ('Customer', RESEED, 0);
DBCC CHECKIDENT ('EventInquiry', RESEED, 0);
DBCC CHECKIDENT ('Vendor', RESEED, 0);
DBCC CHECKIDENT ('Venue', RESEED, 0);
DBCC CHECKIDENT ('Event', RESEED, 0);
DBCC CHECKIDENT ('Invoice', RESEED, 0);
DBCC CHECKIDENT ('Feedback', RESEED, 0);
DBCC CHECKIDENT ('Complaint', RESEED, 0);
DBCC CHECKIDENT ('EventVendor', RESEED, 0);
DBCC CHECKIDENT ('VendorAvailability', RESEED, 0);
DBCC CHECKIDENT ('VenueAvailability', RESEED, 0);

-- 3. INSERT DATA IN CORRECT ORDER

-- 3.1 Insert Customers FIRST (they have no dependencies)
INSERT INTO Customer (Customer_Name, Phone, Email, Password) VALUES
('John Smith', '555-0101', 'john.smith@email.com', 'pass123'),
('Sarah Johnson', '555-0102', 'sarah.j@email.com', 'pass123'),
('Mike Wilson', '555-0103', 'mike.wilson@email.com', 'pass123'),
('Emily Davis', '555-0104', 'emily.davis@email.com', 'pass123'),
('Robert Brown', '555-0105', 'robert.b@email.com', 'pass123');

-- 3.2 Insert Vendors (no dependencies)
INSERT INTO Vendor (VendorName, VendorType, Price, ContactNo) VALUES
('Elite Photography', 'Photographer', 1200.00, '555-1001'),
('Capture Moments', 'Photographer', 950.00, '555-1002'),
('Lens Masters', 'Photographer', 1500.00, '555-1003'),
('Gourmet Delights', 'Caterer', 2500.00, '555-2001'),
('Food Paradise', 'Caterer', 1800.00, '555-2002'),
('Royal Catering', 'Caterer', 3200.00, '555-2003'),
('DJ Beat Master', 'DJ', 800.00, '555-3001'),
('Sound Waves', 'DJ', 650.00, '555-3002'),
('Party Mixer', 'DJ', 950.00, '555-3003'),
('Dream Decor', 'Decorator', 1500.00, '555-4001'),
('Elegant Designs', 'Decorator', 2200.00, '555-4002'),
('Creative Spaces', 'Decorator', 1200.00, '555-4003');

-- 3.3 Insert Venues (no dependencies)
INSERT INTO Venue (Name, Location, VenueCost, Capacity, Availability) VALUES
('Grand Ballroom', 'Downtown City Center', 5000.00, 200, 'Available'),
('Garden Pavilion', 'Riverside Park', 3500.00, 150, 'Available'),
('Luxury Hall', 'Uptown District', 7500.00, 300, 'Available'),
('Beachside Venue', 'Coastal Area', 6000.00, 180, 'Available'),
('Mountain View Lodge', 'Hill Station', 4200.00, 120, 'Available');

-- 3.4 Insert EventInquiries (depends on Customer - uses CustomerID 1-5)
INSERT INTO EventInquiry (Budget, ProposedDate, GuestCount, SpecialRequests, CustomerID, 
                         Photographer, Catering, DJ, Decorations, EventType, Status) VALUES
(15000.00, '2024-02-15', 120, 'Outdoor ceremony with floral arrangements', 1, 1, 1, 1, 1, 'Wedding', 'Pending'),
(8000.00, '2024-02-20', 80, 'Corporate theme with branding', 2, 1, 1, 0, 1, 'Corporate', 'Pending'),
(12000.00, '2024-02-25', 100, 'Birthday party with cake and balloons', 3, 1, 1, 1, 1, 'Birthday', 'Pending'),
(20000.00, '2024-03-01', 180, 'Large wedding with live band', 4, 1, 1, 1, 1, 'Wedding', 'Pending'),
(5000.00, '2024-03-05', 60, 'Small intimate gathering', 5, 1, 0, 1, 0, 'Anniversary', 'Pending');

-- 3.5 Insert Events (depends on Customer, Venue, EventInquiry)
INSERT INTO Event (EventType, EventDate, CustomerID, VenueID, InquiryID, Status) VALUES
('Wedding', '2024-02-15', 1, 1, 1, 'Pending'),
('Corporate', '2024-02-20', 2, 2, 2, 'Pending'),
('Birthday', '2024-02-25', 3, NULL, 3, 'Pending'),
('Wedding', '2024-03-01', 4, NULL, 4, 'Pending'),
('Anniversary', '2024-03-05', 5, NULL, 5, 'Pending');

-- 3.6 Insert EventVendor assignments (depends on Event, Vendor)
INSERT INTO EventVendor (EventID, VendorID, ServiceType) VALUES
(1, 1, 'Photographer'),
(1, 4, 'Caterer'),
(2, 2, 'Photographer'),
(2, 5, 'Caterer');

-- 3.7 Insert VendorAvailability blocks (depends on Vendor, Event)
INSERT INTO VendorAvailability (VendorID, UnavailableDate, EventID, Reason) VALUES
(1, '2024-02-15', 1, 'Event Booking'),
(4, '2024-02-15', 1, 'Event Booking'),
(2, '2024-02-20', 2, 'Event Booking'),
(5, '2024-02-20', 2, 'Event Booking'),
(3, '2024-02-10', NULL, 'Personal Leave'),
(6, '2024-02-18', NULL, 'Maintenance');

-- 3.8 Insert Invoices (depends on Event, Customer)
INSERT INTO Invoice (IssueDate, VerificationStatus, EventID, CustomerID, Total_Pay, PaymentStatus) VALUES
('2024-01-20', 'Verified', 1, 1, 12000.00, 'Paid'),
('2024-01-22', 'Pending', 2, 2, 8500.00, 'Pending'),
('2024-01-25', 'Verified', 3, 3, 9500.00, 'Paid');

-- 3.9 Insert Feedback (depends on Event, Customer)
INSERT INTO Feedback (IssueDescription, Rating, Comments, EventID, CustomerID, Status, Response) VALUES
('Great service overall', 5, 'The event was perfectly organized', 1, 1, 'Addressed', 'Thank you for your feedback!'),
('Catering could be better', 3, 'Food was cold when served', 2, 2, 'Pending', NULL),
('Excellent photography', 5, 'Photos came out amazing!', 1, 1, 'Addressed', 'We are glad you liked our photography service');

-- 3.10 Insert Complaints (depends on Customer)
INSERT INTO Complaint (Description, DateSubmitted, CustomerID, Status, Response) VALUES
('Venue was not cleaned properly', '2024-01-18', 2, 'Resolved', 'We apologize and have addressed this with our cleaning team'),
('Vendor arrived late', '2024-01-22', 3, 'Pending', NULL);

-- 4. VERIFY ALL DATA INSERTED CORRECTLY
SELECT 
    (SELECT COUNT(*) FROM Customer) AS CustomerCount,
    (SELECT COUNT(*) FROM Vendor) AS VendorCount,
    (SELECT COUNT(*) FROM Venue) AS VenueCount,
    (SELECT COUNT(*) FROM EventInquiry) AS InquiryCount,
    (SELECT COUNT(*) FROM Event) AS EventCount,
    (SELECT COUNT(*) FROM EventVendor) AS EventVendorCount,
    (SELECT COUNT(*) FROM VendorAvailability) AS AvailabilityCount,
    (SELECT COUNT(*) FROM Invoice) AS InvoiceCount,
    (SELECT COUNT(*) FROM Feedback) AS FeedbackCount,
    (SELECT COUNT(*) FROM Complaint) AS ComplaintCount;

-- 5. VERIFY CUSTOMER-EventInquiry RELATIONSHIP
SELECT c.CustomerID, c.Customer_Name, ei.InquiryID, ei.EventType, ei.ProposedDate
FROM Customer c
JOIN EventInquiry ei ON c.CustomerID = ei.CustomerID
ORDER BY c.CustomerID;

SELECT InquiryID, CustomerID, EventType, Status, ProposedDate 
FROM EventInquiry ;

SELECT * FROM SystemUsers
SELECT * FROM Vendor
SELECT * FROM Customer