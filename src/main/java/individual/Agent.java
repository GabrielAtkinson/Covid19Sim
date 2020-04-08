package individual;

import pop.Population;

public class Agent {

    private int idx;
    private VirusState state;
    private int dayInfected;
    private double lengthInfected;

    public Agent(VirusState state){
        this.state = state;
    }

    public Agent(VirusState state, double lengthInfected){
        this.state = state;
        this.dayInfected = 0;
        this.lengthInfected = state.equals(VirusState.INFECTED) ? lengthInfected : 0;
    }

    public void spread(Population pop){
        state.spread(pop, idx);
    }

    public void infect(int day, double lengthInfected){
        if (state.equals(VirusState.INFECTED)) return;

        state = state.infect();
        this.lengthInfected = lengthInfected;
        this.dayInfected = day;
    }

    public void remove(Population population){
        if (state.equals(VirusState.INFECTED)) {
            int diff = population.currentDay() - dayInfected;
            if (diff >= Math.floor(lengthInfected)){
                if (population.getRandom().nextFloat() >= lengthInfected - diff){
                    state = state.next();                }
            }
        }
    }

    public void setState(VirusState state) {
        this.state = state;
    }

    public VirusState getState() {
        return state;
    }

    public boolean isInfected(){
        return state == VirusState.INFECTED;
    }

    public boolean notSusceptible(){
        return !(state == VirusState.SUSCEPTIBLE);
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

}
