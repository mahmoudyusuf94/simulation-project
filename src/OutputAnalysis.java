import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputAnalysis {
    private static int W = 6000;

    public static void main(String[] args) throws IOException {
//        applyMovingAverage("q1");
//        applyMovingAverage("q2");
//        applyMovingAverage("q3");

        compareSettings();
    }

    private static void compareSettings() throws IOException {

        List<Double> X11 = new ArrayList<>();
        List<Double> X12 = new ArrayList<>();
        List<Double> X13 = new ArrayList<>();

        List<Double> X21 = new ArrayList<>();
        List<Double> X22 = new ArrayList<>();
        List<Double> X23 = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("X1i"))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> lineStrs = Arrays.asList(line.split("\\s*,\\s*"));
                X11.add(Double.parseDouble(lineStrs.get(0)));
                X12.add(Double.parseDouble(lineStrs.get(1)));
                X13.add(Double.parseDouble(lineStrs.get(2)));
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader("X2i"))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> lineStrs = Arrays.asList(line.split("\\s*,\\s*"));
                X21.add(Double.parseDouble(lineStrs.get(0)));
                X22.add(Double.parseDouble(lineStrs.get(1)));
                X23.add(Double.parseDouble(lineStrs.get(2)));
            }
        }

        int n = 10;

        List<Double> Z1 = new ArrayList<>();
        List<Double> Z2 = new ArrayList<>();
        List<Double> Z3 = new ArrayList<>();

        double z1Bar = 0;
        double z2Bar = 0;
        double z3Bar = 0;

        for(int i = 0 ; i < n ; i++){
            Z1.add(X11.get(i) - X21.get(i));
            z1Bar += X11.get(i) - X21.get(i);

            Z2.add(X12.get(i) - X22.get(i));
            z2Bar += X12.get(i) - X22.get(i);

            Z3.add(X13.get(i) - X23.get(i));
            z3Bar += X13.get(i) - X23.get(i);
        }

        z1Bar /= n;
        z2Bar /= n;
        z1Bar /= n;

        double v1 = 0;
        double v2 = 0;
        double v3 = 0;

        for (int j = 0; j < n ; j++){
            v1 += Math.pow( Z1.get(j) - z1Bar, 2);
            v2 += Math.pow( Z2.get(j) - z2Bar, 2);
            v3 += Math.pow( Z3.get(j) - z3Bar, 2);
        }

        v1 /= (n * (n - 1));
        v2 /= (n * (n - 1));
        v3 /= (n * (n - 1));

//        double t = 4.604; // dof = 4 , alpha/2 = 0.005  , 99% confidence
        double t = 3.250; // dof = 9 , alpha/2 = 0.005  , 99% confidence
        double c1Min = z1Bar + t * Math.sqrt(v1);
        double c1Max = z1Bar - t * Math.sqrt(v1);

        double c2Min = z2Bar + t * Math.sqrt(v2);
        double c2Max = z2Bar - t * Math.sqrt(v2);

        double c3Min = z3Bar + t * Math.sqrt(v3);
        double c3Max = z3Bar - t * Math.sqrt(v3);

        System.out.println("Average Queue1 Waiting Time -> [ "+ c1Min +", "+ c1Max +" ]");
        System.out.println("Average Queue2 Waiting Time -> [ "+ c2Min +", "+ c2Max +" ]");
        System.out.println("Average Queue3 Waiting Time -> [ "+ c3Min +", "+ c3Max +" ]");

    }

    private static void applyMovingAverage(String fileName) throws IOException {
        int m = Integer.MAX_VALUE;
        int n = 0;

        List<List<Integer>> replications = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> lineStrs = Arrays.asList(line.split("\\s*,\\s*"));
                List<Integer> lineInts = new ArrayList<>();
                for(String item: lineStrs){
                    lineInts.add(Integer.parseInt(item));
                }
                n++;
                replications.add(lineInts);
                if(lineInts.size() < m) m = lineInts.size();
            }
            System.out.println(n);
        }

        double[] averages = new double[m];
        for(List<Integer> replication : replications){
            for(int i = 0 ; i< m ; i++){
                averages[i] += replication.get(i);
            }
        }

        for (int i = 0 ; i < averages.length ; i++) {
            averages[i] = averages[i] / n;
        }

        double[] movingAverage = new double[m];

        for(int i = 0; i < m - W; i ++){
            if(i < W){

                double tmp = 0;
                for(int s = -i; s<= i; s++){
                    tmp += averages[i+s];
                }
                movingAverage[i] =  tmp / (2 * i - 1);

            }else{

                double tmp = 0 ;
                for (int s = -W; s <= W; s++){
                    tmp += averages[i+s];
                }
                movingAverage[i] = tmp / (2 * W + 1);

            }
        }

        FileWriter fw = new FileWriter(fileName + "-moving-average-"+W);
        for(int i = 0 ; i < movingAverage.length-1 ; i++){
            fw.write(movingAverage[i] + ", ");
        }
        fw.write(movingAverage[movingAverage.length-1]+"");
        fw.close();
    }
}
