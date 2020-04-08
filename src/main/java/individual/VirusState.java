package individual;


import pop.Population;

import java.util.Random;

public enum VirusState {
    SUSCEPTIBLE,
    INFECTED,
    REMOVED;

    boolean spread(Population pop, int idx){
        try {
            Agent toInfect = pop.select(idx);
            if (this.equals(INFECTED) && pop.getRandom().nextInt(100) < 100*(pop.getG0() - 1)){
                toInfect.infect(pop.currentDay(), pop.sampleLength());
                return true;
            }
            return false;
        } catch (IllegalArgumentException e){
            return false;
        }
    }

    VirusState infect(){
        if(this.equals(SUSCEPTIBLE)){
            return INFECTED;
        }
        return this;
    }

    VirusState next(){
        if (this.equals(SUSCEPTIBLE)) {
            return INFECTED;
        }
        return REMOVED;
    }



}
