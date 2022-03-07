import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Constructor extends Machine{

    private int interval;
    private int constructorID;
    private int capacity;
    OreType type;
    private int producedCount = 0;
    private int resourceCount = 0;
    private int resUseRate;
    private int transporterCount = 0;
    boolean stopped = false;
    ReentrantLock lock;
    Condition empty;
    Condition full;
    HW2Logger logger = HW2Logger.getInstance();

    Constructor(int interval,int capacity,int type){
        this.interval = interval;
        this.capacity = capacity;

        if(type==1){
            resUseRate = 1;
        }
        else{
            resUseRate = 2;
        }
        lock = new ReentrantLock();
        empty = lock.newCondition();
        full = lock.newCondition();
        setType(2);
    }

    public void setConstructorID(int constructorID) {
        this.constructorID = constructorID;
        this.setId(constructorID);
    }


    public void waitCanProduce() throws InterruptedException{
        lock.lock();
        while(resourceCount<resUseRate ){

            if(getInTransporters()==0){
                break;
            }

            empty.await();


        }

        lock.unlock();
    }

    public void constructorProduced() throws InterruptedException{
        lock.lock();
        //System.out.println("Produced\n");
        producedCount++;
        resourceCount -= resUseRate;
        full.signalAll();
        lock.unlock();

    }
    public void constructorStopped(){
        stopped = true;

    }
    @Override
    public void run() {
        logger.Log(0,0,0,constructorID,Action.CONSTRUCTOR_CREATED);
        try{
            while((resourceCount>=resUseRate || getInTransporters()!=0)){

                waitCanProduce();
                logger.Log(0,0,0,constructorID,Action.CONSTRUCTOR_STARTED);
                Simulator.sleepRandom(interval);
                logger.Log(0,0,0,constructorID,Action.CONSTRUCTOR_FINISHED);
                constructorProduced();
            }
            logger.Log(0,0,0,constructorID,Action.CONSTRUCTOR_STOPPED);
            constructorStopped();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void unloadResource(){
        resourceCount++;
    }
    public int getConstructorID() {
        return constructorID;
    }

    @Override
    public boolean hasProducts() {
        return false;
    }

    @Override
    public boolean isActive() {
        return !stopped;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public int getResUseRate() {
        return resUseRate;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Condition getEmpty() {
        return empty;
    }

    public Condition getFull() {
        return full;
    }

    public int getCapacity() {
            return capacity;
    }
}
