import java.io.Serializable;

public class BattleResult implements Serializable {
    private int p1CardValue;
    private int p2CardValue;

    public BattleResult(int p1CardValue, int p2CardValue) {
        this.p1CardValue = p1CardValue;
        this.p2CardValue = p2CardValue;
    }

    public int getP1CardValue() {
        return p1CardValue;
    }

    public int getP2CardValue() {
        return p2CardValue;
    }
}
