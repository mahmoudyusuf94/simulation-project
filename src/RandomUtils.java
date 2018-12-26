import java.util.Random;

public class RandomUtils {

    private Random random;

    public RandomUtils(){
        random = new Random();
    }

    public double getRandomUniform(){
        double val = random.nextDouble();
        return val;
    }

    public double getRandomUniformInRange(double min, double max){
        return min + (max - min) * random.nextDouble();
    }

    public  double getExponential(double mean){
        return (Math.log(1 - getRandomUniform())) * mean * -1;
    }

}
