import pop.Population;

import java.util.Scanner;

public class Sim {

    public static void main(String[] args) {
        double G0 = 1.30; // Base growth rate

        // Following a Weibull Distribution
        double Ym = 5.8; // Incubation Duration mean
        double Ysd = 2.8; // Incubation Duration standard dev
        double Sm = 3.4; // Symptom Onset To Isolation Duration mean
        double Ssd = 2.4; // Symptom Onset To Isolation Duration standard dev
        int n = 0;
        int r = 0;

        if (args.length < 2){
            System.err.println("Requires at least 2 args");
            return;
        }
        int simLength;
        int popSize;

        try {
            switch (args.length){
                case 9:
                    Ssd = Double.parseDouble(args[8]);
                case 8:
                    Sm = Double.parseDouble(args[7]);
                case 7:
                    Ysd = Double.parseDouble(args[6]);
                case 6:
                    Ym = Double.parseDouble(args[5]);
                case 5:
                    r = Integer.parseInt(args[4]);
                case 4:
                    n = Integer.parseInt(args[3]);
                case 3:
                    G0 = Double.parseDouble(args[2]);
                case 2:
                    simLength = Integer.parseInt(args[0]);
                    popSize = Integer.parseInt(args[1]);
                    break;
                default:
                    System.err.println("Requires at least 2 args");
                    return;
            }
        } catch (NumberFormatException e){
            System.err.println("Invalid Input");
            e.printStackTrace();
            return;
        }

        Population p;
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Would you like to update the community sizes? (y/n)");
            String change = sc.nextLine().toLowerCase();
            if(change.equals("n")) {
                p = new Population(simLength, popSize, G0, Ym, Ysd, Sm, Ssd, n, r);
            } else if (change.equals("y")){
                String[] input;

                do {
                    System.out.println("Enter <Isolation Group size (int)> " +
                            "<community size (int)> " +
                            "<probability of containment in the isolation group (double)>");
                    input = sc.nextLine().split(" ");
                    if (input[0].toLowerCase().equals("exit")) return;
                } while (input.length != 3);

                int innerCommSize = Integer.parseInt(input[0]);
                int outerCommunitySize = Integer.parseInt(input[1]);
                double probNonContain = 1 - Double.parseDouble(input[2]);

                if(innerCommSize > outerCommunitySize || Double.compare(probNonContain, 0) < 0 ||
                        Double.compare(probNonContain, 1) > 0 || outerCommunitySize > popSize || popSize < 0){
                    System.err.println("Ensure population sizes are aligned");
                    return;
                }

                p = new Population(simLength, popSize, G0, Ym, Ysd, Sm, Ssd, n, r, innerCommSize, outerCommunitySize, probNonContain);
            } else {
                System.out.println("Invalid Input");
                return;
            }

        }catch (NumberFormatException e){
            System.err.println("incorrect number format");
            return;
        }

        while (p.simDay());
        p.plot();
    }
}
