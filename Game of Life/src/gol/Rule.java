package gol;

public final class Rule {
    private final boolean[] birth = new boolean[9];
    private final boolean[] survive = new boolean[9];

    private Rule() {}

    public static Rule parse(String spec) {
        String s = spec.toUpperCase().replaceAll("\\s+", "");
        Rule r = new Rule();

        if (!s.contains("B") && !s.contains("S") && s.contains("/")) {
            String[] parts = s.split("/");
            s = "S" + parts[0] + "/B" + parts[1];
        }
        for (String part : s.split("/")) {
            if (part.isEmpty()) continue;
            boolean isB = part.startsWith("B");
            boolean isS = part.startsWith("S");
            String digits = (isB || isS) ? part.substring(1) : part;
            for (char c : digits.toCharArray()) {
                if (c < '0' || c > '8') continue;
                int n = c - '0';
                if (isB) r.birth[n] = true; else if (isS) r.survive[n] = true;
            }
        }
        return r;
    }

    public boolean birth(int n) { return n >= 0 && n <= 8 && birth[n]; }
    public boolean survive(int n) { return n >= 0 && n <= 8 && survive[n]; }

    public String toString() {
        return "B" + digits(birth) + "/S" + digits(survive);
    }
    private static String digits(boolean[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 8; i++) if (a[i]) sb.append(i);
        return sb.toString();
    }
}
