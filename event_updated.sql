-- Drop tables in reverse order of dependencies (if they exist)
DROP TABLE IF EXISTS Complaint;
DROP TABLE IF EXISTS Feedback;
DROP TABLE IF EXISTS Invoice;
DROP TABLE IF EXISTS EventVendor;
DROP TABLE IF EXISTS VendorAvailability;
DROP TABLE IF EXISTS VenueAvailability; -- Add this new table to drop list
DROP TABLE IF EXISTS Event;
DROP TABLE IF EXISTS EventInquiry;
DROP TABLE IF EXISTS Vendor;
DROP TABLE IF EXISTS Venue;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS SystemUsers;

-- Creating customer table with auto-increment
CREATE TABLE Customer (
    CustomerID INT IDENTITY(1,1) PRIMARY KEY,
    Customer_Name VARCHAR(100) NOT NULL,
    Phone VARCHAR(20),
    Email VARCHAR(100),
    Password VARCHAR(10)
);

-- Customer inquiries
CREATE TABLE EventInquiry (
    InquiryID INT IDENTITY(1,1) PRIMARY KEY,
    Budget DECIMAL(12,2),
    ProposedDate DATE NOT NULL,
    GuestCount INT,
    SpecialRequests VARCHAR(500),
    CustomerID INT NOT NULL,
    Photographer BIT,
    Catering BIT,
    DJ BIT,
    Decorations BIT,
    EventType VARCHAR(50),
    Status VARCHAR(20) DEFAULT 'Pending',
    CONSTRAINT FK_EventInquiry_Customer 
        FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Create vendor table
CREATE TABLE Vendor (
    VendorID INT IDENTITY(1,1) PRIMARY KEY,
    VendorName VARCHAR(100) NOT NULL,
    VendorType VARCHAR(50) NOT NULL CHECK (VendorType IN ('Photographer', 'DJ', 'Caterer', 'Decorator')),
    Price DECIMAL(12,2) NOT NULL,
    ContactNo VARCHAR(20) NOT NULL,
    IsActive BIT DEFAULT 1
);

-- Vendor availability table for date-specific blocking
CREATE TABLE VendorAvailability (
    AvailabilityID INT IDENTITY(1,1) PRIMARY KEY,
    VendorID INT NOT NULL,
    UnavailableDate DATE NOT NULL,
    EventID INT NULL,
    Reason VARCHAR(100) DEFAULT 'Event Booking',
    CONSTRAINT FK_VendorAvailability_Vendor FOREIGN KEY (VendorID) REFERENCES Vendor(VendorID),
    CONSTRAINT FK_VendorAvailability_Event FOREIGN KEY (EventID) REFERENCES Event(EventID),
    CONSTRAINT UQ_Vendor_Date UNIQUE (VendorID, UnavailableDate)
);

-- Creating venue table
CREATE TABLE Venue (
    VenueID INT IDENTITY(1,1) PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Location VARCHAR(255),
    VenueCost DECIMAL(12,2),
    Capacity INT,
    Availability VARCHAR(20) DEFAULT 'Available'  -- Keep for quick filtering, but not for actual availability logic
);

-- NEW: Venue availability table for date-specific blocking (MIRRORING VENDOR SYSTEM)
CREATE TABLE VenueAvailability (
    AvailabilityID INT IDENTITY(1,1) PRIMARY KEY,
    VenueID INT NOT NULL,
    UnavailableDate DATE NOT NULL,
    EventID INT NULL,
    Reason VARCHAR(100) DEFAULT 'Event Booking',
    CONSTRAINT FK_VenueAvailability_Venue FOREIGN KEY (VenueID) REFERENCES Venue(VenueID),
    CONSTRAINT FK_VenueAvailability_Event FOREIGN KEY (EventID) REFERENCES Event(EventID),
    CONSTRAINT UQ_Venue_Date UNIQUE (VenueID, UnavailableDate) -- Prevent double-booking same venue on same date
);

-- Creating the event table
CREATE TABLE Event (
    EventID INT IDENTITY(1,1) PRIMARY KEY,
    EventType VARCHAR(50) NOT NULL,
    EventDate DATE NOT NULL,  -- This is crucial for availability checks
    CustomerID INT NOT NULL,
    VenueID INT NULL,
    InquiryID INT NULL,
    Status VARCHAR(20) DEFAULT 'Pending',
    CONSTRAINT FK_Event_Customer 
        FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Event_Venue 
        FOREIGN KEY (VenueID) REFERENCES Venue(VenueID),
    CONSTRAINT FK_Event_Inquiry 
        FOREIGN KEY (InquiryID) REFERENCES EventInquiry(InquiryID)
);

-- Create EventVendor junction table for multiple vendors per event
CREATE TABLE EventVendor (
    EventVendorID INT IDENTITY(1,1) PRIMARY KEY,
    EventID INT NOT NULL,
    VendorID INT NOT NULL,
    ServiceType VARCHAR(50) NOT NULL CHECK (ServiceType IN ('Photographer', 'Caterer', 'DJ', 'Decorator')),
    AssignedDate DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_EventVendor_Event FOREIGN KEY (EventID) REFERENCES Event(EventID),
    CONSTRAINT FK_EventVendor_Vendor FOREIGN KEY (VendorID) REFERENCES Vendor(VendorID),
    CONSTRAINT UQ_Event_ServiceType UNIQUE (EventID, ServiceType)
);

-- (Rest of your tables remain the same)
CREATE TABLE Invoice (
    InvoiceID INT IDENTITY(1,1) PRIMARY KEY,
    IssueDate DATE NOT NULL,
    VerificationStatus VARCHAR(20) DEFAULT 'Pending',
    EventID INT NOT NULL,
    CustomerID INT NOT NULL,
    Total_Pay Decimal(12,2),
    PaymentStatus VARCHAR(20) DEFAULT 'Pending',
    CONSTRAINT FK_Invoice_Event FOREIGN KEY (EventID) REFERENCES Event(EventID),
    CONSTRAINT FK_Invoice_Customer FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE Feedback (
    FeedbackID INT IDENTITY(1,1) PRIMARY KEY,
    IssueDescription VARCHAR(500),
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Comments VARCHAR(500),
    EventID INT NOT NULL,
    CustomerID INT NOT NULL,
    DateSubmitted DATE DEFAULT GETDATE(),
    Status VARCHAR(20) DEFAULT 'New',
    Response VARCHAR(500),
    CONSTRAINT FK_Feedback_Event FOREIGN KEY (EventID) REFERENCES Event(EventID),
    CONSTRAINT FK_Feedback_Customer FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE Complaint (
    ComplaintID INT IDENTITY(1,1) PRIMARY KEY,
    Description VARCHAR(500),
    DateSubmitted DATE DEFAULT GETDATE(),
    CustomerID INT NOT NULL,
    Status VARCHAR(20) DEFAULT 'New',
    Response VARCHAR(500),
    CONSTRAINT FK_Complaint_Customer FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

-- Add finance columns to existing Invoice table (one by one)
ALTER TABLE Invoice ADD VenueCost DECIMAL(12,2) DEFAULT 0;
ALTER TABLE Invoice ADD VendorCost DECIMAL(12,2) DEFAULT 0;
ALTER TABLE Invoice ADD AdditionalCharges DECIMAL(12,2) DEFAULT 0;
ALTER TABLE Invoice ADD TaxAmount DECIMAL(12,2) DEFAULT 0;
ALTER TABLE Invoice ADD ServiceCharges DECIMAL(12,2) DEFAULT 0;
ALTER TABLE Invoice ADD OtherCharges DECIMAL(12,2) DEFAULT 0;
ALTER TABLE Invoice ADD AdditionalComments VARCHAR(500);
ALTER TABLE Invoice ADD GeneratedDate DATETIME DEFAULT GETDATE();
ALTER TABLE Invoice ADD DueDate DATE;

CREATE TABLE SystemUsers (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    UserRole VARCHAR(50) NOT NULL CHECK (UserRole IN (
        'CustomerConsultant', 'EventCoordinator', 'VenueCoordinator', 
        'VendorCoordinator', 'FinanceOfficer', 'CustomerSupportOfficer'
    )),
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE(),
    LastLogin DATETIME NULL
);

SELECT * FROM SystemUsers
SELECT * FROM Event
SELECT * FROM Invoice
SELECT * FROM Customer
SELECT * FROM Feedback
SELECT * FROM Complaint