package it.unige.dibris.mas.gui;

import javafx.scene.chart.XYChart;

public class ColorStats {
    public String color;
    public XYChart.Data<String, Number> data;
    
    public ColorStats(String color, XYChart.Data<String, Number> data) {
        this.color = color;
        this.data = data;
    }
    
    public int getCount() {
        return ((Number) data.getYValue()).intValue();
    }
    
    public void setCount(int value) {
        data.setYValue(value);
    }
}