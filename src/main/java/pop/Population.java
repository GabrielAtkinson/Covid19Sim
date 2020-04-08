package pop;

import individual.Agent;
import individual.VirusState;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.special.Gamma;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.List;

public class Population {
    static final int COMM_SIZE = 3;
    static final int IND_COMM_SIZE = 1000;
    static final double PROB_OUT = 0.07;
    private Agent[] pop;
    Random r;


    // Parameters
    private double[] Y; // Incubation Duration // Weibull - Mean CI 5.8 (5.2, 6.5) SD  2.8 (2.3, 3.5), (Linton, 2020)
    private double[] D; // Incubation To Isolation Duration Weibull Y U S
    private double[] S;  // Symptom Onset To Isolation Duration | Weibull Mean 3.4 (2.7, 4.2) SD 4.4 (3.3, 6.0)
    private WeibullDistribution dRand;

    private final double[] R;
    private final double[] G;

    private final int[] totalInfections;
    private final int[] activeCases;

    private int day;
    private int simLength;
    public Population(int days, int popSize, double G0, double Y, double Ysd, double S, double Ssd, int iN, int rN){
        this.Y = new double[]{Y, Ysd};
        this.S = new double[]{S, Ssd};
        this.D = new double[]{S + Y, Math.sqrt(Math.pow(Ysd, 2) + Math.pow(Ssd, 2))};

        //Weibull D
        double sigNoise = D[0]/D[1];
        double m = 1.2785 * sigNoise - 0.5004;
        double scale = D[0]/(Gamma.gamma(1+1/m));

        dRand = new WeibullDistribution(new JDKRandomGenerator(), m, scale);

        G = new double[days];
        R = new double[days];

        totalInfections = new int[days];
        activeCases = new int[days];
        this.pop = new Agent[popSize];
        for (int i = 0; i < pop.length; i++) {
            pop[i] = new Agent(VirusState.SUSCEPTIBLE);
        }

        this.simLength = days;
        R[0] = Math.pow(G0, S);
        G[0] = G0;
        totalInfections[0] = iN;
        activeCases[0] = iN-rN;
        printDay();
        r = new Random();
        initInfectedAndRemoved(iN, rN);
        shufflePop();

        for (int i = 0; i < pop.length; i++) {
            pop[i].setIdx(i < COMM_SIZE ? 0 : i - COMM_SIZE/2);
        }

        this.day = 1;
    }

    private void shufflePop(){
        List<Agent> popL = Arrays.asList(pop);
        Collections.shuffle(popL);
        pop = popL.toArray(pop);
    }

    public Agent select(int bucketIdx){
        return pop[Double.compare(r.nextDouble(), PROB_OUT) > 0 ?
                Math.min(r.nextInt(COMM_SIZE) + bucketIdx, pop.length - 1) :
                Math.min(r.nextInt(IND_COMM_SIZE) + bucketIdx, pop.length - 1)];
    }

    public double getG0() {
        return G[0];
    }

    public int currentDay() {
        return day;
    }

    public double sampleLength() {
        return dRand.sample();
    }

    private void initInfectedAndRemoved(int n, int r) {
        for (int i = 0; i < n-r; i++) {
            pop[i] = new Agent(VirusState.INFECTED, sampleLength());
        }
        for (int i = n-r; i < n; i++) {
            pop[i] = new Agent(VirusState.REMOVED);
        }
        totalInfections[0] = n;
        activeCases[0] = n-r;
    }

    public boolean simDay(){
        Arrays.stream(pop).filter(agent -> agent.getState().equals(VirusState.INFECTED)).forEach( a -> {
            a.spread(this);
            a.remove(this);
        });

        activeCases[day] = (int)Arrays.stream(pop).filter(Agent::isInfected).count();
        totalInfections[day] = (int)Arrays.stream(pop).filter(Agent::notSusceptible).count();
        G[day] = (double)totalInfections[day]/totalInfections[day - 1];
        R[day] = Math.pow((double)totalInfections[day]/totalInfections[day - 1], D[0]);

        printDay();
        day++;
        return day < activeCases.length;
    }

    public void printDay(){
        String print = String.format(
          "Day: %d\nRt: %.2f, Gt: %.2f\nTotal Cases: %d\nActive Cases: %d\n New Cases: %d",
                day, R[day], G[day], totalInfections[day], activeCases[day], day > 0 ? totalInfections[day] - totalInfections[day - 1] : -1
        );

        System.out.println(print);
    }

    public Random getRandom() {
        return r;
    }

    public void plot(){

        String chartTitle = "COVID-19 Sim";
        String xAxisLabel = "Time";
        String yAxisLabel = "Cases";

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries totalCasesSeries = new XYSeries("Total Cases");
        XYSeries activeCasesSeries = new XYSeries("Active Cases");
        for (int i = 0; i < day ; i++) {
            activeCasesSeries.add(i,activeCases[i]);
            totalCasesSeries.add(i, totalInfections[i]);
        }

        dataset.addSeries(totalCasesSeries);
        dataset.addSeries(activeCasesSeries);
        JFreeChart chart = ChartFactory.createXYLineChart(
                chartTitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL,
                true, true, false
        );

        File imageFile = new File(String.format("./outputs/Sim%.2f_%d_%d-%d_%d_%.2f.png", G[0], pop.length, COMM_SIZE, IND_COMM_SIZE, currentDay(), PROB_OUT));
        int width = 640;
        int height = 480;

        try {
            ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    public double getR0() {
        return R[0];
    }

}
