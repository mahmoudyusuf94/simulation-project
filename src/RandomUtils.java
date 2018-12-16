import java.util.Random;

public final class RandomUtils {

    private static final Random random = new Random();

    public static double getRandomUniform(){
        return random.nextDouble();
    }

    public static double getRandomUniformInRange(double min, double max){
        return min + (max - min) * random.nextDouble();
    }

    public static double getExponential(double mean){
        return (Math.log(1 - getRandomUniform()))/(-mean);
    }

}
