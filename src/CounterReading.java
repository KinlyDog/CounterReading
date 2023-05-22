public class CounterReading {
    private int id;
    private String apartment;
    private double coldWater;
    private double hotWater;
    private double electricity;
    private double gas;

    public CounterReading() {
    }

    public CounterReading(String apartment, double coldWater, double hotWater, double electricity, double gas) {
        this.apartment = apartment;
        this.coldWater = coldWater;
        this.hotWater = hotWater;
        this.electricity = electricity;
        this.gas = gas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public double getColdWater() {
        return coldWater;
    }

    public void setColdWater(double coldWater) {
        this.coldWater = coldWater;
    }

    public double getHotWater() {
        return hotWater;
    }

    public void setHotWater(double hotWater) {
        this.hotWater = hotWater;
    }

    public double getElectricity() {
        return electricity;
    }

    public void setElectricity(double electricity) {
        this.electricity = electricity;
    }

    public double getGas() {
        return gas;
    }

    public void setGas(double gas) {
        this.gas = gas;
    }
}
