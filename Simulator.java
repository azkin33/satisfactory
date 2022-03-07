
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.DoubleStream;

public class Simulator {
    private static List<Miner> miners;
    private static List<Smelter> smelters;
    private static List<Constructor> constructors;
    private static List<Transporter> transporters;
    private static int threadCount = 0;
    public static int minerStarted = 0;
    public static int minerFinished = 0;
    public static void incrementMinerStarted(){
        minerStarted++;
    }
    public static void sleepRandom(int interval){
        try{
            Random random = new Random(System.currentTimeMillis());
            DoubleStream stream;
            stream = random.doubles(1, interval-interval*0.01, interval+interval*0.02);
            Thread.sleep((long) stream.findFirst().getAsDouble());

        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    private static void readInit(BufferedReader reader) throws IOException {
        int minerCount,smelterCount,constructorCount,transporterCount;
        Miner miner;
        Smelter smelter;
        Constructor constructor;
        Transporter transporter;
        String str = reader.readLine();
        minerCount = Integer.parseInt(str);
        for(int i=1;i<=minerCount;i++){
            String[] input = reader.readLine().split(" ");
            int interval = Integer.parseInt(input[0]);
            int capacity = Integer.parseInt(input[1]);
            int oreType = Integer.parseInt(input[2]);
            int totalOre = Integer.parseInt(input[3]);

            miner = new Miner(interval,capacity,oreType,totalOre);
            miner.setMinerID(i);
            miners.add(miner);
        }

        str = reader.readLine();
        smelterCount = Integer.parseInt(str);
        for(int i=1;i<=smelterCount;i++){
            String[] input = reader.readLine().split(" ");
            int interval = Integer.parseInt(input[0]);
            int inCapacity = Integer.parseInt(input[1]);
            int opCapacity = Integer.parseInt(input[2]);
            int oreType = Integer.parseInt(input[3]);
            smelter = new Smelter(interval,inCapacity,opCapacity,oreType);
            smelter.setSmelterID(i);
            smelters.add(smelter);
        }

        str = reader.readLine();
        constructorCount = Integer.parseInt(str);
        for(int i=1;i<=constructorCount;i++){
            String[] input = reader.readLine().split(" ");
            int interval = Integer.parseInt(input[0]);
            int capacity = Integer.parseInt(input[1]);
            int type = Integer.parseInt(input[2]);
            constructor = new Constructor(interval,capacity,type);
            constructor.setConstructorID(i);
            constructors.add(constructor);
        }

        str = reader.readLine();
        transporterCount = Integer.parseInt(str);
        for(int i=1;i<=transporterCount;i++){
            String[] input = reader.readLine().split(" ");
            int interval = Integer.parseInt(input[0]);
            int sourceMiner = Integer.parseInt(input[1]);
            int sourceSmelter = Integer.parseInt(input[2]);
            int targetSmelter = Integer.parseInt(input[3]);
            int targetConstructor = Integer.parseInt(input[4]);
            if(sourceMiner!=0){
                if(targetSmelter!=0){
                    transporter = new Transporter(interval,miners.get(sourceMiner-1),smelters.get(targetSmelter-1));
                    smelters.get(targetSmelter-1).incTransporters();
                }
                else{
                    transporter = new Transporter(interval,miners.get(sourceMiner-1),constructors.get(targetConstructor-1));
                    constructors.get(targetConstructor-1).incTransporters();
                }
            }
            else{
                transporter = new Transporter(interval,smelters.get(sourceSmelter-1),constructors.get(targetConstructor-1));
                constructors.get(targetConstructor-1).incTransporters();
            }
            transporter.setTransporterID(i);
            transporters.add(transporter);
        }
        threadCount = minerCount+smelterCount+constructorCount+transporterCount;
    }
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        miners = new ArrayList<Miner>();
        smelters = new ArrayList<Smelter>();
        constructors = new ArrayList<Constructor>();
        transporters = new ArrayList<Transporter>();
        try{
            readInit(reader);
            ExecutorService es = Executors.newFixedThreadPool(threadCount);
            for(int i=0;i<miners.size();i++){
                es.execute(miners.get(i));
            }
            for(int i=0;i<smelters.size();i++){
                es.execute(smelters.get(i));
            }
            for(int i=0;i<constructors.size();i++){
                es.execute(constructors.get(i));
            }
            for(int i=0;i<transporters.size();i++){
                es.execute(transporters.get(i));
            }

            es.shutdown();
        }
        catch(IOException e){
            e.printStackTrace();
        }


    }
}
