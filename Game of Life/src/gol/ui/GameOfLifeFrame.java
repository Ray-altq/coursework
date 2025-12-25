package gol.ui;

import gol.GameOfLifeModel;
import gol.Pattern;
import javax.swing.event.ChangeListener;
import gol.Rule;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameOfLifeFrame extends JFrame {
    private final GameOfLifeModel model = new GameOfLifeModel();
    private final BoardPanel board = new BoardPanel(model);

    // пресеты правил
    private enum RulePreset {
        LIFE("Life (B3/S23)", "B3/S23"),
        HIGHLIFE("HighLife (B36/S23)", "B36/S23"),
        DAY_NIGHT("Day&Night (B3678/S34678)", "B3678/S34678"),
        SEEDS("Seeds (B2/S)", "B2/S"),
        LWD("Life w/o Death (B3/S012345678)", "B3/S012345678");
        final String title, spec;
        RulePreset(String title, String spec){ this.title=title; this.spec=spec; }
    }



    private final JCheckBox torusCheck = new JCheckBox("Torus");
    private final JSpinner torusW = new JSpinner(new SpinnerNumberModel(200, 5, 10000, 10));
    private final JSpinner torusH = new JSpinner(new SpinnerNumberModel(200, 5, 10000, 10));

    private final JComboBox<Pattern> patternBox = new JComboBox<>(Pattern.values());
    private final JToggleButton stampToggle = new JToggleButton("Stamp");

    private final JSlider speed = new JSlider(1, 60, 10);
    private final JButton startPause = new JButton("Start");
    private final JButton stepBtn = new JButton("Step");
    private final JButton resetBtn = new JButton("Reset");
    private final JButton randomBtn = new JButton("Random");


    private Timer timer;

    public GameOfLifeFrame() {
        super("Conway's Game of Life");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Верхняя панель: 2 строки =====
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        row1.setBorder(new EmptyBorder(6,6,0,6));
        row2.setBorder(new EmptyBorder(0,6,6,6));

//row 1 - Rule
        row1.add(new JLabel("Rule:"));
        ButtonGroup rulesGroup = new ButtonGroup();
        for (RulePreset rp : RulePreset.values()) {
            JToggleButton b = new JToggleButton(rp.title);
            b.addActionListener(e -> { model.setRule(Rule.parse(rp.spec)); board.repaint(); });
            rulesGroup.add(b);
            row1.add(b);
            if (rp == RulePreset.LIFE) b.setSelected(true);
        }

// row 2 - other
        row2.add(new JLabel("Speed:"));
        speed.setPaintTicks(true); speed.setMajorTickSpacing(10);
        row2.add(speed);
        row2.add(startPause);
        row2.add(stepBtn);
        row2.add(resetBtn);
        row2.add(randomBtn);
        row2.add(torusCheck);
        row2.add(new JLabel("W:"));
        ((JSpinner.DefaultEditor)torusW.getEditor()).getTextField().setColumns(3);
        row2.add(torusW);
        row2.add(new JLabel("H:"));
        ((JSpinner.DefaultEditor)torusH.getEditor()).getTextField().setColumns(3);
        row2.add(torusH);


        row2.add(new JLabel("  Pattern:"));
        row2.add(patternBox);
        row2.add(stampToggle);

        // контейнер для строк
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(row1);
        controls.add(row2);
        add(controls, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);


        // ===== Таймер =====
        timer = new Timer(1000 / speed.getValue(), e -> { model.step(); board.repaint(); });

        // ===== Логика =====
        startPause.addActionListener(e -> {
            if (timer.isRunning()) { timer.stop(); model.setRunning(false); startPause.setText("Start"); }
            else {
                timer.setDelay(Math.max(1, 1000 / speed.getValue()));
                timer.start(); model.setRunning(true); startPause.setText("Pause");
            }
        });
        speed.addChangeListener(e -> timer.setDelay(Math.max(1, 1000 / Math.max(1, speed.getValue()))));
        stepBtn.addActionListener(e -> { if (!timer.isRunning()) { model.step(); board.repaint(); } });
        resetBtn.addActionListener(e -> { timer.stop(); model.setRunning(false); startPause.setText("Start"); model.clear(); board.repaint(); });
        randomBtn.addActionListener(e -> {
            int cells = 400;   // сколько одиночных клеток
            int patterns = 6;  // сколько паттернов
            java.util.Random rnd = new java.util.Random();

            int minX, minY, maxX, maxY;
            if (torusCheck.isSelected()) {
                minX = 0; minY = 0;
                maxX = model.getTorusWidth() - 1;
                maxY = model.getTorusHeight() - 1;
            } else {
                Rectangle r = board.getVisibleGridBounds();
                minX = r.x - r.width / 10;
                minY = r.y - r.height / 10;
                maxX = r.x + r.width + r.width / 10;
                maxY = r.y + r.height + r.height / 10;
            }
            model.randomize(cells, patterns, minX, minY, maxX, maxY, rnd);
            board.repaint();
        });




        // Топология переключение
        torusCheck.addActionListener(e -> {
            boolean on = torusCheck.isSelected();
            if (on) {
                model.setTopology(GameOfLifeModel.Topology.TORUS);
                model.setTorusSize((Integer) torusW.getValue(), (Integer) torusH.getValue());
            } else {
                model.setTopology(GameOfLifeModel.Topology.INFINITE);
            }
        });
        ChangeListener wh = e -> {
            if (torusCheck.isSelected()) {
                model.setTorusSize((Integer) torusW.getValue(), (Integer) torusH.getValue());


            }
            if (torusCheck.isSelected()) {
                model.setTopology(GameOfLifeModel.Topology.TORUS);
                model.setTorusSize((Integer) torusW.getValue(), (Integer) torusH.getValue());
            } else {
                model.setTopology(GameOfLifeModel.Topology.INFINITE);

            }

        };
        torusW.addChangeListener(wh);
        torusH.addChangeListener(wh);

        // Паттерн-штамп
        stampToggle.addActionListener(e -> {
            if (stampToggle.isSelected()) board.setCurrentPattern((Pattern) patternBox.getSelectedItem());
            else board.setCurrentPattern(null);
        });
        patternBox.addActionListener(e -> {
            if (stampToggle.isSelected()) board.setCurrentPattern((Pattern) patternBox.getSelectedItem());
        });

        setSize(1300, 720);
        setLocationRelativeTo(null);

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameOfLifeFrame().setVisible(true));
    }
}
