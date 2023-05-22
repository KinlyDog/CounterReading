import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:counters.db");
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public void createTable(String tableName) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "apartment TEXT," +
                "cold_water REAL," +
                "hot_water REAL," +
                "electricity REAL," +
                "gas REAL" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public void addCounterReading(String tableName, CounterReading counterReading) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (apartment, cold_water, hot_water, electricity, gas) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, counterReading.getApartment());
            statement.setDouble(2, counterReading.getColdWater());
            statement.setDouble(3, counterReading.getHotWater());
            statement.setDouble(4, counterReading.getElectricity());
            statement.setDouble(5, counterReading.getGas());
            statement.executeUpdate();
        }
    }

    public void updateCounterReading(String tableName, CounterReading counterReading) throws SQLException {
        String sql = "UPDATE " + tableName + " SET apartment = ?, cold_water = ?, hot_water = ?, electricity = ?, gas = ? WHERE id = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, counterReading.getApartment());
            statement.setDouble(2, counterReading.getColdWater());
            statement.setDouble(3, counterReading.getHotWater());
            statement.setDouble(4, counterReading.getElectricity());
            statement.setDouble(5, counterReading.getGas());
            statement.setInt(6, counterReading.getId());
            statement.executeUpdate();
        }
    }

    public void deleteTable(String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName + ";";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public List<CounterReading> getCounterReadings(String tableName) throws SQLException {
        List<CounterReading> readings = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                double coldWater = resultSet.getDouble("cold_water");
                double hotWater = resultSet.getDouble("hot_water");
                double electricity = resultSet.getDouble("electricity");
                double gas = resultSet.getDouble("gas");
                CounterReading reading = new CounterReading(tableName, coldWater, hotWater, electricity, gas);
                readings.add(reading);
            }
        }
        return readings;
    }

    public void importTable(String tableName, String csvFile) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (apartment, cold_water, hot_water, electricity, gas) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    statement.setString(1, line[0]);
                    statement.setDouble(2, Double.parseDouble(line[1]));
                    statement.setDouble(3, Double.parseDouble(line[2]));
                    statement.setDouble(4, Double.parseDouble(line[3]));
                    statement.setDouble(5, Double.parseDouble(line[4]));
                    statement.executeUpdate();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CsvValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void exportTable(String tableName, String csvFile) throws SQLException {
        String sql = "SELECT * FROM " + tableName;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql);
             FileWriter writer = new FileWriter(csvFile)) {

            int columnCount = resultSet.getMetaData().getColumnCount();

            // Write column headers
            for (int i = 1; i <= columnCount; i++) {
                writer.append(resultSet.getMetaData().getColumnLabel(i));
                if (i < columnCount) {
                    writer.append(",");
                }
            }
            writer.append("\n");

            // Write data rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(resultSet.getString(i));
                    if (i < columnCount) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getTableNames() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"});
        List<String> tableNames = new ArrayList<>();
        while (resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            tableNames.add(tableName);
        }
        resultSet.close();
        return tableNames.toArray(new String[0]);
    }
}
