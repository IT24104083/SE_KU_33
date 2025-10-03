package com.example.test002.service;

import com.example.test002.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Create a new customer - using exact column names
    public Customer createCustomer(Customer customer) {
        String sql = "INSERT INTO Customer (Customer_Name, Phone, Email, Password) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPassword());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            customer.setCustomerID(keyHolder.getKey().intValue());
        }

        return customer;
    }

    // Get all customers - using exact column names
    public List<Customer> getAllCustomers() {
        String sql = "SELECT CustomerID, Customer_Name, Phone, Email, Password FROM Customer ORDER BY CustomerID";
        return jdbcTemplate.query(sql, new CustomerRowMapper());
    }

    // Get customer by ID - using exact column names
    public Customer getCustomerById(Integer id) {
        String sql = "SELECT CustomerID, Customer_Name, Phone, Email, Password FROM Customer WHERE CustomerID = ?";
        return jdbcTemplate.queryForObject(sql, new CustomerRowMapper(), id);
    }

    // Check if email already exists
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Customer WHERE Email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    // RowMapper for Customer - using exact column names
    private static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Customer customer = new Customer();
            customer.setCustomerID(rs.getInt("CustomerID"));
            customer.setCustomerName(rs.getString("Customer_Name")); // Exact column name
            customer.setPhone(rs.getString("Phone"));
            customer.setEmail(rs.getString("Email"));
            customer.setPassword(rs.getString("Password"));
            return customer;
        }
    }

    // Additional method: Get customers count
    public int getCustomersCount() {
        String sql = "SELECT COUNT(*) FROM Customer";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}