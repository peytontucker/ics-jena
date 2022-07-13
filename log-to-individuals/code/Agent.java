import java.util.ArrayList;

public class Agent {
    private int ID;
    private int stance;
    private ArrayList<Integer> receivedIPList;
    private ArrayList<Integer> sentIPList;

    public Agent(int ID, int stance, ArrayList<Integer> receivedIPList, ArrayList<Integer> sentIPList) {
        this.ID = ID;
        this.stance = stance;
        this.receivedIPList = receivedIPList;
        this.sentIPList = sentIPList;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public void setReceivedIPList(ArrayList<Integer> receivedIPList) {
        this.receivedIPList = receivedIPList;
    }

    public void setSentIPList(ArrayList<Integer> sentIPList) {
        this.sentIPList = sentIPList;
    }

    public int getID(){
        return this.ID;
    }

    public int getStance(){
        return this.stance;
    }

    public ArrayList<Integer> getSentIPList(){
        return this.sentIPList;
    }

    public ArrayList<Integer> getReceivedIPList(){
        return this.receivedIPList;
    }

}
