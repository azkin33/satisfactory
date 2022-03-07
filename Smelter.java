import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Smelter extends Machine{
    private ReentrantLock lock;
    private Condition oresEmpty;
    private Condition oresFull;
    private Condition ingotsEmpty;
    private Condition ingotsFull;
    private int smelterID;
    private int inCapacity;
    private int opCapacity;
    private int oreCount = 0;
    private int interval;
    private int ingotCount = 0;
    private OreType oreType;
    private int oreUseRate;
    boolean stopped = false;
    HW2Logger logger = HW2Logger.getInstance();

    Smelter(int interval,int inCapacity,int opCapacity,int type){
        this.interval = interval;
        this.inCapacity = inCapacity;
        this.opCapacity = opCapacity;
        if(type==0){
            oreType = OreType.IRON;
        }
        else if(type==1){
            oreType = OreType.COPPER;
        }
        else{
            oreType = OreType.LIMESTONE;
        }
        lock = new ReentrantLock();
        oresEmpty = lock.newCondition();
        oresFull = lock.newCondition();
        ingotsEmpty = lock.newCondition();
        ingotsFull = lock.newCondition();
        if(oreType == OreType.IRON) oreUseRate = 1;
        else oreUseRate = 2;
        setType(1);
    }

    public void setSmelterID(int smelterID) {
        this.smelterID = smelterID;
        this.setId(smelterID);
    }
    public void waitCanProduce() throws InterruptedException{
        lock.lock();
        while(oreCount<oreUseRate){
            if(getInTransporters()==0){
                break;
            }
            oresEmpty.await();
        }
        lock.unlock();
    }
    public void ingotProduced(){
        lock.lock();
        ingotCount++;
        oreCount -= oreUseRate;
        System.out.println(oreCount);
        ingotsEmpty.signalAll();
        oresFull.signalAll();
        lock.unlock();
    }
    public void smelterStopped(){
        stopped = true;
    }

    public void load(){
        ingotCount--;
    }

    public void unloadOre(){
        oreCount++;
    }
    @Override
    public void run() {
        logger.Log(0,smelterID,0,0,Action.SMELTER_CREATED);
       try{
           //System.out.println(oreCount+" has ores\n");
           while((getInTransporters()!=0 || oreCount>=oreUseRate)){
               waitCanProduce();
               logger.Log(0,smelterID,0,0,Action.SMELTER_STARTED);
               Simulator.sleepRandom(interval);
               logger.Log(0,smelterID,0,0,Action.SMELTER_FINISHED);
               ingotProduced();
           }
           smelterStopped();
           logger.Log(0,smelterID,0,0,Action.SMELTER_STOPPED);
       }
       catch(InterruptedException e){
           e.printStackTrace();
       }

    }

    public int getSmelterID() {
        return smelterID;
    }

    public boolean isStopped() {
        return stopped;
    }

    public int getIngotCount() {
        return ingotCount;
    }

    @Override
    public boolean hasProducts() {
        return ingotCount>0;
    }

    @Override
    public boolean isActive() {
        return !stopped;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Condition getOresEmpty() {
        return oresEmpty;
    }

    public Condition getOresFull() {
        return oresFull;
    }

    public Condition getIngotsEmpty() {
        return ingotsEmpty;
    }

    public Condition getIngotsFull() {
        return ingotsFull;
    }

    public int getOreCount() {
        return oreCount;
    }

    public int getInCapacity() {
        return inCapacity;
    }

    public int getOreUseRate() {
        return oreUseRate;
    }
}
