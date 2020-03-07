package ch.epfl.gameboj.component;

/**
 * Interface Clocked
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public interface Clocked {

    /**
     * makes components evolve according to the cycle
     * 
     * @param cycle
     */
    void cycle(long cycle);
}
