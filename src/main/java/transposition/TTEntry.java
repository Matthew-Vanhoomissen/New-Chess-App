package transposition;

public class TTEntry {
    public static final int EXACT = 0;  // exact score
    public static final int LOWER = 1;  // alpha cutoff (lower bound)
    public static final int UPPER = 2;  // beta cutoff (upper bound)

    public float score;
    public int depth;
    public int flag;

    public TTEntry(float score, int depth, int flag) {
        this.score = score;
        this.depth = depth;
        this.flag = flag;
    }
}