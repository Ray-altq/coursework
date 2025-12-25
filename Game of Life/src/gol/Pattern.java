package gol;

import java.util.List;

/** Набор готовых паттернов. Координаты даны относительно (0,0). */
public enum Pattern {
    GLIDER(List.of(  // глайдер
            new int[]{0,0}, new int[]{1,0}, new int[]{2,0},
            new int[]{2,1},
            new int[]{1,2}
    )),
    BLINKER(List.of(
            new int[]{0,0}, new int[]{1,0}, new int[]{2,0}
    )),
    TOAD(List.of(
            new int[]{1,0}, new int[]{2,0}, new int[]{3,0},
            new int[]{0,1}, new int[]{1,1}, new int[]{2,1}
    )),
    BEACON(List.of(
            new int[]{0,0}, new int[]{1,0}, new int[]{0,1},
            new int[]{3,3}, new int[]{2,3}, new int[]{3,2}
    )),
    LWSS(List.of(
            new int[]{1,0}, new int[]{4,0},
            new int[]{0,1},
            new int[]{0,2}, new int[]{4,2},
            new int[]{0,3}, new int[]{1,3}, new int[]{2,3}, new int[]{3,3}
    )),

    PULSAR(List.of(
            new int[]{2,0}, new int[]{3,0}, new int[]{4,0},
            new int[]{8,0}, new int[]{9,0}, new int[]{10,0},
            new int[]{0,2}, new int[]{5,2}, new int[]{7,2}, new int[]{12,2},
            new int[]{0,3}, new int[]{5,3}, new int[]{7,3}, new int[]{12,3},
            new int[]{0,4}, new int[]{5,4}, new int[]{7,4}, new int[]{12,4},
            new int[]{2,5}, new int[]{3,5}, new int[]{4,5},
            new int[]{8,5}, new int[]{9,5}, new int[]{10,5},
            // нижняя половина — симметрия
            new int[]{2,7}, new int[]{3,7}, new int[]{4,7},
            new int[]{8,7}, new int[]{9,7}, new int[]{10,7},
            new int[]{0,8}, new int[]{5,8}, new int[]{7,8}, new int[]{12,8},
            new int[]{0,9}, new int[]{5,9}, new int[]{7,9}, new int[]{12,9},
            new int[]{0,10}, new int[]{5,10}, new int[]{7,10}, new int[]{12,10},
            new int[]{2,12}, new int[]{3,12}, new int[]{4,12},
            new int[]{8,12}, new int[]{9,12}, new int[]{10,12}
    )),
    GOSPER_GLIDER_GUN(List.of( // планер-пушка
            new int[]{1,5},new int[]{1,6},new int[]{2,5},new int[]{2,6},
            new int[]{13,3},new int[]{14,3},new int[]{12,4},new int[]{16,4},new int[]{11,5},new int[]{17,5},new int[]{11,6},new int[]{15,6},new int[]{17,6},new int[]{18,6},new int[]{11,7},new int[]{17,7},new int[]{12,8},new int[]{16,8},new int[]{13,9},new int[]{14,9},
            new int[]{25,1},new int[]{23,2},new int[]{25,2},new int[]{21,3},new int[]{22,3},new int[]{21,4},new int[]{22,4},new int[]{21,5},new int[]{22,5},new int[]{23,6},new int[]{25,6},new int[]{25,7},
            new int[]{35,3},new int[]{36,3},new int[]{35,4},new int[]{36,4}
    ));


    public final List<int[]> cells;
    Pattern(List<int[]> cells) { this.cells = cells; }
}
