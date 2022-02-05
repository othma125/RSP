





/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.ui.ApplicationFrame;
public class GanttDemoCollection extends ApplicationFrame{
    public GanttDemoCollection(String title,HeuristicSolution s){
        super(title);
        IntervalCategoryDataset dataset=s.createDataset();
        JFreeChart chart=createChart(dataset);
        ChartPanel chartPanel=new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500,270));
        setContentPane(chartPanel);
    }
    private JFreeChart createChart(IntervalCategoryDataset dataset) {
        JFreeChart chart=ChartFactory.createGanttChart(
            "",    // chart title
            "",              // domain axis label
            "",              // range axis label
            dataset,          // data
            true,            // include legend
            true,            // tooltips
            false            // urls
        ); 
        CategoryPlot plot=(CategoryPlot)chart.getPlot();
//        chart.getPlot().setBackgroundPaint(Color.WHITE);
        plot.setRangeAxis(new NumberAxis());
        ((BarRenderer) plot.getRenderer()).setItemMargin(-1d);
        return chart;    
    }
}