package gol;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

public class GameOfLifeModel {
    public enum Topology { INFINITE, TORUS }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Set<Long> alive = new HashSet<>();
    private Map<Long, Integer> age = new HashMap<>();
    private Rule rule = Rule.parse("B3/S23");

    //топология тора
    private Topology topology = Topology.INFINITE;
    private int torusWidth = 200, torusHeight = 150; // по умолчанию, можно менять из UI

    private boolean running = false;
    private int generation = 0;

    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }

    public int getGeneration() { return generation; }
    public Rule getRule() { return rule; }
    public void setRule(Rule rule) { this.rule = Objects.requireNonNull(rule); pcs.firePropertyChange("rule", null, this.rule); }

    public Set<Long> getAliveSnapshot() { return Set.copyOf(alive); }
    public int getAge(long packed) { return age.getOrDefault(packed, 0); }


    public Topology getTopology() { return topology; }
    public void setTopology(Topology t) { this.topology = Objects.requireNonNull(t); }
    public void setTorusSize(int w, int h) { this.torusWidth = Math.max(5, w); this.torusHeight = Math.max(5, h); }
    public int getTorusWidth() { return torusWidth; }
    public int getTorusHeight() { return torusHeight; }

    public void setRunning(boolean running) { this.running = running; }
    public boolean isRunning() { return running; }

    public void clear() {
        alive.clear(); age.clear(); generation = 0;
        pcs.firePropertyChange("cells", null, null);
        pcs.firePropertyChange("generation", null, generation);
    }

    // Логика Тора
    private int wrapX(int x) {
        if (topology != Topology.TORUS) return x;
        int m = torusWidth;
        int r = x % m; if (r < 0) r += m; return r;
    }
    private int wrapY(int y) {
        if (topology != Topology.TORUS) return y;
        int m = torusHeight;
        int r = y % m; if (r < 0) r += m; return r;
    }

    public void toggleCell(int x, int y) { setCell(x, y, !isAlive(x, y)); }
    public boolean isAlive(int x, int y) { return alive.contains(pack(wrapX(x), wrapY(y))); }

    public void setCell(int x, int y, boolean aliveState) {
        int nx = wrapX(x), ny = wrapY(y);
        long p = pack(nx, ny);
        if (aliveState) { if (alive.add(p)) age.put(p, 1); }
        else { alive.remove(p); age.remove(p); }
        pcs.firePropertyChange("cells", null, null);
    }
    // === случайная генерация ===
    public void randomize(int cells, int patterns, int minX, int minY, int maxX, int maxY, Random rnd) {
        // случайные одиночные клетки
        for (int i = 0; i < cells; i++) {
            int x = minX + rnd.nextInt(Math.max(1, maxX - minX + 1));
            int y = minY + rnd.nextInt(Math.max(1, maxY - minY + 1));
            setCell(x, y, true);
        }
        // случайные паттерны
        Pattern[] ps = Pattern.values();
        for (int j = 0; j < patterns; j++) {
            Pattern p = ps[rnd.nextInt(ps.length)];
            int x = minX + rnd.nextInt(Math.max(1, maxX - minX + 1));
            int y = minY + rnd.nextInt(Math.max(1, maxY - minY + 1));
            stampPattern(p, x, y, true);
        }
        pcs.firePropertyChange("cells", null, null);
    }

    // --- «штамповка» паттерна с якорной точкой (gx, gy) ---
    public void stampPattern(Pattern pattern, int gx, int gy, boolean place) {
        for (int[] c : pattern.cells) {
            int x = gx + c[0];
            int y = gy + c[1];
            setCell(x, y, place);
        }
    }

    public void step() {
        Map<Long, Integer> neighbor = new HashMap<>(alive.size() * 8 + 16);

        for (long p : alive) {
            int x = unpackX(p), y = unpackY(p);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = wrapX(x + dx), ny = wrapY(y + dy);
                    long q = pack(nx, ny);
                    neighbor.merge(q, 1, Integer::sum);
                }
            }
        }

        Set<Long> nextAlive = new HashSet<>();
        Map<Long, Integer> nextAge = new HashMap<>();
        Set<Long> candidates = new HashSet<>(neighbor.keySet());
        candidates.addAll(alive);

        for (long p : candidates) {
            int n = neighbor.getOrDefault(p, 0);
            boolean currentlyAlive = alive.contains(p);
            boolean willLive = (!currentlyAlive && rule.birth(n)) || (currentlyAlive && rule.survive(n));
            if (willLive) {
                nextAlive.add(p);
                int newAge = currentlyAlive ? age.getOrDefault(p, 0) + 1 : 1;
                nextAge.put(p, newAge);
            }
        }

        alive = nextAlive; age = nextAge; generation++;
        pcs.firePropertyChange("cells", null, null);
        pcs.firePropertyChange("generation", null, generation);
    }

    // упаковка координат
    public static long pack(int x, int y) { return ((long) x << 32) ^ (y & 0xffffffffL); }
    public static int unpackX(long p) { return (int) (p >> 32); }
    public static int unpackY(long p) { return (int) p; }
}
