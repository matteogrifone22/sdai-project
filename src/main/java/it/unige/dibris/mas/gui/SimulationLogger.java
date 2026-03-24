package it.unige.dibris.mas.gui;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;

public class SimulationLogger {

    // Singleton logger to collect and display simulation events in the GUI

    private static SimulationLogger instance;
    private List<Runnable> listeners = new ArrayList<>();
    private StringBuilder logBuffer = new StringBuilder();
    
    private SimulationLogger() {}
    
    public static SimulationLogger getInstance() {
        if (instance == null) {
            instance = new SimulationLogger();
        }
        return instance;
    }
    
    public void log(String message) {
        logBuffer.append(message).append("\n");
        System.out.println(message);  
        
        Platform.runLater(() -> {
            listeners.forEach(Runnable::run);
        });
    }
    
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }
    
    public String getFullLog() {
        return logBuffer.toString();
    }
}