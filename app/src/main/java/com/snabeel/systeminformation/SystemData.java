package com.snabeel.systeminformation;

public class SystemData {
    String internetConnection;
    String charging;
    String lastBatteryStatus;
    String charged;
    String location;
    String memoryEmpty;
    String date;

    public SystemData(){

    }

    public SystemData(String internetConnection, String charging, String lastBatteryStatus, String charged, String location, String memoryEmpty, String date) {
        this.internetConnection = internetConnection;
        this.charging = charging;
        this.lastBatteryStatus = lastBatteryStatus;
        this.charged = charged;
        this.location = location;
        this.memoryEmpty = memoryEmpty;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInternetConnection() {
        return internetConnection;
    }

    public void setInternetConnection(String internetConnection) {
        this.internetConnection = internetConnection;
    }

    public String getCharging() {
        return charging;
    }

    public void setCharging(String charging) {
        this.charging = charging;
    }

    public String getLastBatteryStatus() {
        return lastBatteryStatus;
    }

    public void setLastBatteryStatus(String lastBatteryStatus) {
        this.lastBatteryStatus = lastBatteryStatus;
    }

    public String getCharged() {
        return charged;
    }

    public void setCharged(String charged) {
        this.charged = charged;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMemoryEmpty() {
        return memoryEmpty;
    }

    public void setMemoryEmpty(String memoryEmpty) {
        this.memoryEmpty = memoryEmpty;
    }
}
