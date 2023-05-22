import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;
import java.util.Optional;


public class MainApp {
    private final JFrame frame;
    private final JComboBox<String> apartmentComboBox;
    private final JButton submitButton;
    private final JTextField coldWaterTextField;
    private final JTextField hotWaterTextField;
    private final JTextField electricityTextField;
    private final JTextField gasTextField;
    private final DatabaseManager databaseManager;

    public MainApp() {
        frame = new JFrame("Показания счетчиков");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLayout(new BorderLayout());

        apartmentComboBox = new JComboBox<>();
        apartmentComboBox.addActionListener(e -> {
            String selectedApartment = (String) apartmentComboBox.getSelectedItem();
            showCounterFields(selectedApartment != null);
        });

        coldWaterTextField = new JTextField();
        hotWaterTextField = new JTextField();
        electricityTextField = new JTextField();
        gasTextField = new JTextField();

        submitButton = new JButton("Передать показания");
        submitButton.addActionListener(e -> submitCounterReadings());

        JButton createTableButton = createCreateTableButton();
        JButton editApartmentsButton = createEditApartmentsButton();
        JButton viewReadingsButton = createViewReadingsButton();

        JPanel buttonPanel = createButtonPanel(createTableButton, editApartmentsButton, viewReadingsButton);
        JPanel inputPanel = createInputPanel();

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        databaseManager = new DatabaseManager();
        try {
            databaseManager.connect();
            loadApartments();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при подключении к базе данных: " + ex.getMessage());
        }
    }

    private void submitCounterReadings() {
        String selectedApartment = (String) apartmentComboBox.getSelectedItem();
        if (selectedApartment == null) {
            return;
        }

        double coldWater = Double.parseDouble(coldWaterTextField.getText());
        double hotWater = Double.parseDouble(hotWaterTextField.getText());
        double electricity = Double.parseDouble(electricityTextField.getText());
        double gas = Double.parseDouble(gasTextField.getText());

        CounterReading counterReading = new CounterReading(selectedApartment, coldWater, hotWater, electricity, gas);
        try {
            databaseManager.addCounterReading(selectedApartment, counterReading);
            JOptionPane.showMessageDialog(frame, "Показания счетчиков переданы успешно");
            clearInputFields();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка при передаче показаний счетчиков: " + ex.getMessage());
        }
    }

    private JButton createCreateTableButton() {
        JButton createTableButton = new JButton("Добавить таблицу");
        createTableButton.addActionListener(e -> {
            String tableName = JOptionPane.showInputDialog(frame, "Введите название квартиры:");
            if (tableName != null && !tableName.isEmpty()) {
                try {
                    databaseManager.createTable(tableName);
                    loadApartments();
                    JOptionPane.showMessageDialog(frame, "Таблица успешно добавлена");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка при добавлении таблицы: " + ex.getMessage());
                }
            }
        });
        return createTableButton;
    }

    private JButton createEditApartmentsButton() {
        JButton editApartmentsButton = new JButton("Редактировать квартиры");
        editApartmentsButton.addActionListener(e -> {
            Optional<String> selectedApartment = selectApartment();

            selectedApartment.ifPresent(this::deleteApartment);
        });
        return editApartmentsButton;
    }

    private Optional<String> selectApartment() {
        String[] tableNames = getApartmentNames();

        return Optional.ofNullable((String) JOptionPane.showInputDialog(
                frame,
                "Выберите квартиру для удаления:",
                "Редактировать квартиры",
                JOptionPane.QUESTION_MESSAGE,
                null, tableNames,
                tableNames.length > 0 ? tableNames[0] : null));
    }

    private String[] getApartmentNames() {
        String[] tableNames = new String[apartmentComboBox.getItemCount()];
        for (int i = 0; i < apartmentComboBox.getItemCount(); i++) {
            tableNames[i] = apartmentComboBox.getItemAt(i);
        }
        return tableNames;
    }

    private void deleteApartment(String apartmentName) {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Вы действительно хотите удалить квартиру " + apartmentName + "?",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                databaseManager.deleteTable(apartmentName);
                loadApartments();
                JOptionPane.showMessageDialog(frame, "Квартира успешно удалена");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка при удалении квартиры: " + ex.getMessage());
            }
        }
    }


    private JButton createViewReadingsButton() {
        JButton viewReadingsButton = new JButton("Просмотр показаний");
        viewReadingsButton.addActionListener(e -> {
            String selectedApartment = (String) apartmentComboBox.getSelectedItem();
            if (selectedApartment != null) {
                try {
                    List<CounterReading> readings = databaseManager.getCounterReadings(selectedApartment);
                    new ReadingsWindow(selectedApartment, readings);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка при получении показаний счетчиков: " + ex.getMessage());
                }
            }
        });
        return viewReadingsButton;
    }

    private JPanel createButtonPanel(JButton createTableButton, JButton editApartmentsButton, JButton viewReadingsButton) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2));
        buttonPanel.add(submitButton);
        buttonPanel.add(createTableButton);
        buttonPanel.add(editApartmentsButton);
        buttonPanel.add(viewReadingsButton);
        return buttonPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 1));
        inputPanel.add(new JLabel("Квартира:"));
        inputPanel.add(apartmentComboBox);
        inputPanel.add(new JLabel("Холодная вода:"));
        inputPanel.add(coldWaterTextField);
        inputPanel.add(new JLabel("Горячая вода:"));
        inputPanel.add(hotWaterTextField);
        inputPanel.add(new JLabel("Электричество:"));
        inputPanel.add(electricityTextField);
        inputPanel.add(new JLabel("Газ:"));
        inputPanel.add(gasTextField);
        return inputPanel;
    }

    private void loadApartments() throws SQLException {
        apartmentComboBox.removeAllItems();

        String[] tableNames = databaseManager.getTableNames();
        for (String tableName : tableNames) {
            apartmentComboBox.addItem(tableName);
        }
    }

    private void showCounterFields(boolean show) {
        if (submitButton != null) {
            submitButton.setEnabled(show);
        }
    }

    private void clearInputFields() {
        coldWaterTextField.setText("");
        hotWaterTextField.setText("");
        electricityTextField.setText("");
        gasTextField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}
