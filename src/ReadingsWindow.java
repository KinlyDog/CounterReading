import javax.swing.*;
import java.util.List;

public class ReadingsWindow {

    public ReadingsWindow(String apartmentName, List<CounterReading> readings) {
        JFrame frame = new JFrame("Показания счетчиков для квартиры " + apartmentName);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        String[] columnNames = {"Холодная вода", "Горячая вода", "Электричество", "Газ"};
        Object[][] data = new Object[readings.size()][4];
        for (int i = 0; i < readings.size(); i++) {
            CounterReading reading = readings.get(i);
            data[i][0] = reading.getColdWater();
            data[i][1] = reading.getHotWater();
            data[i][2] = reading.getElectricity();
            data[i][3] = reading.getGas();
        }

        JTable table = new JTable(data, columnNames);
        frame.add(new JScrollPane(table));

        frame.setVisible(true);
    }
}
