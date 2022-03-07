import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Miner extends Machine{
    private ReentrantLock lock;
    private Condition full;
    private Condition empty;
    private int minerID;
    private int capacity;
    private int totalOre;
    private int interval;
    private OreType oreType;
    private int oreCount = 0;

    HW2Logger logger = HW2Logger.getInstance();
    private boolean stopped = false;
    Miner(int interval,int capacity,int type,int totalOre){
        this.interval = interval;
        this.capacity = capacity;
        if(type==0){
            oreType = OreType.IRON;
        }
        else if(type==1){
            oreType = OreType.COPPER;
        }
        else{
            oreType = OreType.LIMESTONE;
        }
        this.totalOre = totalOre;
        lock = new ReentrantLock();
        full = lock.newCondition();
        empty = lock.newCondition();
        setType(0);

    }

    public int getMinerID() {
        return minerID;
    }

    public void setMinerID(int minerID) {
        this.minerID = minerID;
        this.setId(minerID);
    }

    public int getCapacity() {
        return capacity;
    }

    public OreType getOreType() {
        return oreType;
    }

    public void waitCanProduce() throws InterruptedException{
        lock.lock();
        while(oreCount == capacity){
            full.await();
        }
        lock.unlock();
    }
    public void oreProduced() throws InterruptedException{
        lock.lock();
        oreCount++;
        totalOre--;
        empty.signalAll();
        lock.unlock();
    }
    public void minerStopped() throws InterruptedException{
        stopped = true;
        logger.Log(minerID,0,0,0,Action.MINER_STOPPED);
    }
    public void load(){
        oreCount--;
    }


    @Override
    public void run() {
        logger.Log(minerID,0,0,0,Action.MINER_CREATED);
        try {
            while(totalOre>0){

                waitCanProduce();
                Simulator.incrementMinerStarted();

                logger.Log(minerID,0,0,0,Action.MINER_STARTED);
                Simulator.sleepRandom(interval);
                logger.Log(minerID,0,0,0,Action.MINER_FINISHED);
                oreProduced();
            }
            minerStopped();

        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public int getOreCount() {
        return oreCount;
    }

    public int getTotalOre() {
        return totalOre;
    }

    public boolean isStopped() {
        return stopped;
    }

    @Override
    public boolean hasProducts() {
        return oreCount>0;
    }

    @Override
    public boolean isActive() {
        return !stopped;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Condition getFull() {
        return full;
    }

    public Condition getEmpty() {
        return empty;
    }
    public void stop(){
        stopped = true;
    }
}
