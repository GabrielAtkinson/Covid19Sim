import pop.Population;

public class Sim {

    public static void main(String[] args) {
        if (args.length != 9){
            System.err.println("Requires 9 args");
            return;
        }

        int simLength;
        int popSize;
        double G0;
        double Ym;
        double Ysd;
        double Sm;
        double Ssd;

        int n;
        int r;

        try {
            r = Integer.parseInt(args[8]);
            n = Integer.parseInt(args[7]);
            simLength = Integer.parseInt(args[0]);
            popSize = Integer.parseInt(args[1]);
            G0 = Double.parseDouble(args[2]);
            Ym = Double.parseDouble(args[3]);
            Ysd = Double.parseDouble(args[4]);
            Sm = Double.parseDouble(args[5]);
            Ssd = Double.parseDouble(args[6]);
        } catch (NumberFormatException e){
            System.err.println("Invalid Input");
            e.printStackTrace();
            return;
        }

        Population p = new Population(simLength, popSize, G0, Ym, Ysd, Sm, Ssd, n, r);


        while (p.simDay());
        p.plot();
    }
}
