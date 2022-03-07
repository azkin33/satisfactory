import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Machine implements Runnable{
    private int id;
    private boolean isActive;
    private int type;
    private int inTransporters = 0;



    public void setInTransporters(int inTransporters) {
        this.inTransporters = inTransporters;
    }
    public void incTransporters(){
        inTransporters++;
    }
    public void decTransporters(){
        inTransporters--;
    }

    public int getInTransporters() {
        return inTransporters;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public abstract boolean hasProducts();
    public abstract boolean isActive();
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
