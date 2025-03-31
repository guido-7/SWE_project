package test.testutils;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class ContinuousPDFChart {

    public static void main(String[] args) throws IOException {
        // Crea il grafico
        if (new File("charts").mkdirs()) {
            System.out.println("Directory created");
        }
        else {
            throw new IOException("Directory already exists");
        }
        // Crea la cartella "charts" se non esiste

        int maxCommunity = 12;
        int numTests = 1000; // Numero di simulazioni per ogni k
        int[] kValues = {1, 2, 3, 4, 5, 6}; // Valori di k da testare

        // Genera un grafico per ogni k
        for (int k : kValues) {
            // Simula le frazioni di post dalle community (0.0 - 1.0)
            List<Double> fractions = simulatePostFractions(k, maxCommunity, numTests);

            // Calcola la KDE (Kernel Density Estimation)
            double[][] kde = computeKDE(fractions, 0.05, 100); // Bandwidth = 0.05
            createPDFChart("k_"+k,kde);
        }
    }

    // Simula la frazione di post dalle community (0.0 - 1.0)
    private static List<Double> simulatePostFractions(int k, int maxCommunity, int numTests) {
        Random rand = new Random();
        List<Double> fractions = new ArrayList<>();
        double p = (double) k / maxCommunity; // Probabilità teorica

        // Simula i dati (ad esempio, distribuzione Beta)
        for (int i = 0; i < numTests; i++) {
            // Genera una frazione casuale attorno a p (esempio con Beta distribution)
            double sample = p + rand.nextGaussian() * 0.1; // Aggiungi rumore
            fractions.add(Math.max(0, Math.min(1, sample)));; // Clip tra 0 e 1
        }
        return fractions;
    }

    // Calcola la Kernel Density Estimation (KDE)
    public static double[][] computeKDE(List<Double> data, double bandwidth, int gridSize) {
        double[] x = IntStream.range(0, gridSize).mapToDouble(i -> (double) i / (gridSize - 1)).toArray();
        double[] y = new double[gridSize];

        for (int i = 0; i < gridSize; i++) {
            double sum = 0.0;
            for (double value : data) {
                sum += kernel((x[i] - value) / bandwidth);
            }
            y[i] = sum / (data.size() * bandwidth); // Normalizza
        }
        return new double[][]{x, y};
    }

    // Kernel Gaussiano
    private static double kernel(double u) {
        return Math.exp(-0.5 * u * u) / Math.sqrt(2 * Math.PI);
    }

    public static void createPDFChart(String title, double[][] samples) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title(title)
                .xAxisTitle("Frazione di Post dalla Community")
                .yAxisTitle("Densità di Probabilità")
                .build();

        // Stile del grafico
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

        // Aggiungi la curva di densità
        chart.addSeries("PDF", samples[0], samples[1]);

        // Salva il grafico come PNG
        BitmapEncoder.saveBitmap(chart, "charts/pdf_k_" + title, BitmapEncoder.BitmapFormat.PNG);


    }
}
