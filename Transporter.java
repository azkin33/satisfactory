import java.util.concurrent.locks.Lock;

public class Transporter extends Machine {

    private int transporterID;
    private int sourceType;
    private int targetType;
    private int interval;
    private Miner miner = null;
    private Smelter smelter = null;
    private Constructor constructor = null;
    HW2Logger logger = HW2Logger.getInstance();
    Machine source,target;
    boolean stopped = false;

    public Transporter(int interval,Miner miner,Smelter smelter){
        this.interval = interval;
        source = miner;
        target = smelter;
    }
    public Transporter(int interval,Smelter smelter,Constructor constructor){
        this.interval = interval;
        source = smelter;
        target = constructor;
    }

    public Transporter(int interval, Miner miner, Constructor constructor) {
        this.interval = interval;
        source = miner;
        target = constructor;
    }
    private void waitNextLoad(Machine machine)throws InterruptedException{
        Miner miner;
        Smelter smelter;
        if(machine.getType()==0){
            miner = ((Miner)machine);
            miner.getLock().lock();
            while(miner.getOreCount()==0){
                if(miner.getTotalOre()==0){
                    stopped = true;
                    miner.getLock().unlock();
                    return;
                }
                miner.getEmpty().await();
            }
            miner.getLock().unlock();
        }
        else{
            smelter = ((Smelter)machine);
            smelter.getLock().lock();
            while(smelter.getIngotCount()==0){
                smelter.getIngotsEmpty().await();

            }
            smelter.getLock().unlock();
        }
    }
    private void waitUnload(Machine machine) throws InterruptedException{
        Smelter s;
        Constructor c;
        if(machine.getType()==1){
            s = ((Smelter)machine);
            s.getLock().lock();
            while(s.getOreCount()==s.getInCapacity()){
                s.getOresFull().await();
            }
            s.getLock().unlock();
        }
        else{
            c = ((Constructor)machine);
            c.getLock().lock();
            while(c.getResourceCount()>=c.getCapacity()){
                c.getFull().await();
            }
            c.getLock().unlock();
        }
    }
    private void unloaded(Machine machine) throws InterruptedException{
        Smelter s;
        Constructor c;
        if(machine.getType()==1){
            s = ((Smelter)machine);
            s.getLock().lock();
            s.unloadOre();
            if(s.getOreCount()>=s.getOreUseRate()){
                s.getOresEmpty().signalAll();
            }
            s.getLock().unlock();
        }
        else{
            c = ((Constructor)machine);
            c.getLock().lock();
            c.unloadResource();


            if(c.getResourceCount()>0){
                c.getEmpty().signalAll();
            }
            c.getLock().unlock();
        }
    }

    private void loaded(Machine machine) throws InterruptedException{
        Miner m;
        Smelter s;
        if(machine.getType()==0){
            m = ((Miner)machine);
            m.getLock().lock();
            m.load();
            m.getFull().signalAll();
            m.getLock().unlock();
        }
        else{
            s = ((Smelter)machine);
            s.getLock().lock();
            s.load();
            s.getIngotsFull().signalAll();
            s.getLock().unlock();
        }
    }


    private void sleep(){
        Simulator.sleepRandom(interval);
    }

    @Override
    public void run() {
        logger.Log(0,0,transporterID,0,Action.TRANSPORTER_CREATED);
        if(source.getType()==0){
            logger.Log(source.getId(),0,transporterID,0,Action.TRANSPORTER_GO);
        }
        else{
            logger.Log(0,source.getId(),transporterID,0,Action.TRANSPORTER_GO);
        }
        try{
            while(source.hasProducts() || source.isActive() ){
                sleep();
                if(source.getType()==0){
                    logger.Log(source.getId(),0,transporterID,0,Action.TRANSPORTER_ARRIVE);
                }
                else{
                    logger.Log(0, source.getId(),transporterID,0,Action.TRANSPORTER_ARRIVE);
                }
                waitNextLoad(source);
                if(source.getType()==0){
                    logger.Log(source.getId(),0,transporterID,0,Action.TRANSPORTER_TAKE);
                }
                else{
                    logger.Log(0,source.getId(),transporterID,0,Action.TRANSPORTER_TAKE);
                }
                loaded(source);
                if(source.getType()==0){
                    if(target.getType()==1){
                        logger.Log(source.getId(),target.getId(),transporterID,0,Action.TRANSPORTER_GO);
                    }
                    else{
                        logger.Log(source.getId(),0,transporterID,target.getId(),Action.TRANSPORTER_GO);
                    }
                }
                else{
                    logger.Log(0,source.getId(),transporterID,target.getId(),Action.TRANSPORTER_GO);
                }
                sleep();
                if(target.getType()==1){
                    logger.Log(0,target.getId(),transporterID,0,Action.TRANSPORTER_ARRIVE);
                }
                else{
                    logger.Log(0,0,transporterID,target.getId(),Action.TRANSPORTER_ARRIVE);
                }
                waitUnload(target);
                if(target.getType()==1){
                    logger.Log(0,target.getId(),transporterID,0,Action.TRANSPORTER_DROP);
                }
                else{
                    logger.Log(0,0,transporterID,target.getId(),Action.TRANSPORTER_DROP);
                }
                unloaded(target);
                if(source.getType()==0){
                    if(target.getType()==1){
                        logger.Log(source.getId(),target.getId(),transporterID,0,Action.TRANSPORTER_GO);
                    }
                    else{
                        logger.Log(source.getId(),0,transporterID,target.getId(),Action.TRANSPORTER_GO);
                    }
                }
                else{
                    logger.Log(0,source.getId(),transporterID,target.getId(),Action.TRANSPORTER_GO);
                }
            }
            logger.Log(0,0,transporterID,0,Action.TRANSPORTER_STOPPED);
            if(target.getType()==2){
                ((Constructor)target).getLock().lock();
                ((Constructor)target).getEmpty().signalAll();
                ((Constructor)target).getLock().unlock();
            }
            else{
                ((Smelter)target).getLock().lock();
                ((Smelter)target).getOresEmpty().signalAll();
                ((Smelter)target).getLock().unlock();
            }
            target.decTransporters();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }



    public void setTransporterID(int transporterID) {
        this.transporterID = transporterID;
        setId(transporterID);
    }

    @Override
    public boolean hasProducts() {
        return false;
    }

    @Override
    public boolean isActive() {
        return !stopped;
    }
}
