import com.google.gson.JsonElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.function.Consumer;

public class QuestionComponent extends JPanel {
    private ArrayList<Boolean> answer = new ArrayList<>();
    private int answerCount = 0;
    private Log log = new Log();
    private String questionType = "";
    private Config config = new Config();
    private ArrayList<Consumer<? super String>> check_actions = new ArrayList<>();
    private int score = 0;
    private int max_score = 0;

    public int getScore() {
        return score;
    }

    public int getMax_score() {
        return max_score;
    }

    public void check() {
        answer = new ArrayList<>();
        for (Consumer<? super String> stringConsumer : check_actions) {
            stringConsumer.accept("check");
        }
    }

    public QuestionComponent(StarUI.PaperComponent component) {
        questionType = component.type;
        switch (component.type) {
            case "fill_in_the_blank": {

                setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                for (JsonElement element : component.value) {
                    try {
                        element.getAsString();
                        JLabel text = new JLabel(element.getAsString());
                        text.setFont(new Font(config.getString("settings.font"), Font.PLAIN, text.getFont().getSize()));
                        add(text);
                    } catch (UnsupportedOperationException _) {


                        int maxChars = element.getAsJsonObject().get("maximum_chars").getAsInt() <= 0 ? 20 : element.getAsJsonObject().get("maximum_chars").getAsInt();

                        JTextField field = new JTextField(maxChars);
                        field.setFont(new Font(config.getString("settings.font"), Font.PLAIN, field.getFont().getSize()));

                        Color color = field.getForeground();

                        answerCount++;
                        field.setSize(new Dimension(maxChars * 24, 25));
                        field.setMaximumSize(new Dimension(maxChars * 24, 25));
                        try {
                            element.getAsJsonObject().get("answer").getAsString();
                            check_actions.add(ele -> {
                                if (ele.equals("check")) {
                                    if (field.getText().equals(element.getAsJsonObject().get("answer").getAsString())) {
                                        answer.add(true);
                                        field.setBackground(Color.green);
                                        score += element.getAsJsonObject().get("score").getAsInt();
                                    } else {
                                        answer.add(false);
                                        field.setBackground(Color.red);
                                    }
                                    field.setEnabled(false);
                                }
                            });
                            max_score += element.getAsJsonObject().get("score").getAsInt();
                        } catch (UnsupportedOperationException _) {
                            answerCount--;
                            log.AddLog(Log.ERROR, "Error blank");
                            field.setEnabled(false);
                            field.setText(config.getLang("error_blank"));
                            field.setToolTipText(config.getLang("error_blank"));
                            field.setSize(new Dimension(config.getLang("error_blank").length() * 24, 25));
                            field.setMinimumSize(new Dimension(config.getLang("error_blank").length() * 24, 25));
                            // check_actions.add(ele -> {
                            //     if (ele.equals("check")) {
                            //         log.AddLog(Log.INFO,element.getAsJsonObject()+"");
                            //         ArrayList<String> response = DeepSeekIntegration.getResponse("system",
                            //                 "\n只需回答 \"T\" 或 \"F\", Only answers \"T\"(True) or \"F\"(False)y answer \"T\""+element.getAsJsonObject().get("answer").getAsJsonObject().get("prompt").getAsString().formatted(field.getText()),
                            //                 new Config().getString("settings.api_key"));
                            //         if (response.get(0).equals("200") && (new JsonParser().parse(response.getLast()).getAsJsonObject().get("choices").getAsJsonArray().get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString().equals("T"))) {
                            //             answer.add(true);
                            //         } else {
                            //             answer.add(false);
                            //         }
                            //     }
                            // });
                        }

                        log.AddLog(Log.INFO, field + "Load Done");
                        add(field);

                        field.addKeyListener(new KeyListener() {
                            @Override
                            public void keyTyped(KeyEvent e) {

                            }

                            @Override
                            public void keyPressed(KeyEvent e) {
                                if (field.getText().length() > maxChars) {
                                    field.setText(field.getText().substring(0, maxChars));
                                    new Thread(() -> {
                                        field.setForeground(Color.red);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException _) {
                                            Thread.currentThread().interrupt();
                                            field.setForeground(color);
                                        }
                                        field.setForeground(color);
                                    }).start();
                                }
                            }

                            @Override
                            public void keyReleased(KeyEvent e) {
                                if (field.getText().length() > maxChars) {
                                    field.setText(field.getText().substring(0, maxChars));
                                    new Thread(() -> {
                                        field.setForeground(Color.red);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException _) {
                                            Thread.currentThread().interrupt();
                                            field.setForeground(color);

                                        }
                                        field.setForeground(color);
                                    }).start();
                                }
                            }
                        });
                    }
                }
                break;
            }
            case "application": {

                setLayout(new BorderLayout());
                setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel label = new JLabel(component.question);
                label.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 20));
                add(label, BorderLayout.NORTH);

                JPanel center = new JPanel(new GridLayout(2, 1));

                JTextArea area = new JTextArea();
                area.setFont(new Font(config.getString("settings.font"), Font.PLAIN, area.getFont().getSize()));
                center.add(area);
                JScrollPane scrollPane = new JScrollPane();
                JPanel input_pane = new JPanel();
                input_pane.setLayout(new GridLayout(component.answer.getAsJsonArray().size(), 2));

                for (int i = 0; i < component.answer.getAsJsonArray().size(); i++) {
                    answerCount++;
                    JLabel text = new JLabel(config.getLang("question_number").formatted(i + 1));
                    text.setFont(new Font(config.getString("settings.font"), Font.PLAIN, text.getFont().getSize()));

                    JTextField field = new JTextField();
                    field.setFont(new Font(config.getString("settings.font"), Font.PLAIN, field.getFont().getSize()));

                    max_score += component.score.getAsJsonArray().get(i).getAsInt();

                    int finalI = i;
                    check_actions.add(ele -> {
                        if (ele.equals("check")) {
                            if (field.getText().equals(component.answer.getAsJsonArray().get(finalI).getAsString())) {
                                answer.add(true);
                                field.setBackground(Color.green);
                                score += component.score.getAsJsonArray().get(finalI).getAsInt();
                            } else {
                                answer.add(false);
                                field.setBackground(Color.red);
                            }
                            area.setEnabled(false);
                            field.setEnabled(false);
                        }
                    });

                    input_pane.add(text);
                    input_pane.add(field);
                }
                scrollPane.setViewportView(input_pane);
                center.add(scrollPane);
                add(center, BorderLayout.CENTER);
                break;
            }
            case "single_choice":{
                answerCount++;
                setLayout(new BorderLayout());
                setAlignmentX(Component.LEFT_ALIGNMENT);
                max_score = component.score.getAsInt();
                JLabel l = new JLabel(component.question);
                l.setFont(new Font(config.getString("settings.font"), Font.PLAIN,20));
                JPanel button_panel = new JPanel(new GridLayout(component.options.size(),1));
                RadioGroup buttonGroup = new RadioGroup();
                for (int i = 0; i < component.options.size(); i++) {
                    RadioButton jRadioButton = new RadioButton(component.options.get(i).getFirst() + ".  " + component.options.get(i).getLast(),buttonGroup);

                    try {
                        if (i == component.answer.getAsInt()) {
                            check_actions.add(ele -> {
                                if (ele.equals("check")) {
                                    answer.add(jRadioButton.isSelected());
                                    if (jRadioButton.isSelected()) {
                                        l.setForeground(Color.green);
                                        score = component.score.getAsInt();
                                    } else {
                                        l.setForeground(Color.red);
                                    }
                                }
                                jRadioButton.setEnabled(false);
                            });
                        } else {
                            check_actions.add(ele -> {
                                if (ele.equals("check")) {
                                    jRadioButton.setEnabled(false);
                                }
                            });
                        }

                        buttonGroup.add(jRadioButton);
                        button_panel.add(jRadioButton);
                    } catch (UnsupportedOperationException _) {
                        JLabel t = new JLabel(config.getLang("error"));
                        t.setToolTipText(config.getLang("error"));
                        button_panel.add(t);
                        answerCount = 0;
                    }
                    jRadioButton.setFont(new Font(config.getString("settings.font"), Font.PLAIN,jRadioButton.getFont().getSize()));
                }
                add(button_panel);
                add(l,BorderLayout.NORTH);
                break;
            }
            case "multiple_choice": {
                answerCount++;
                setLayout(new BorderLayout());
                setAlignmentX(Component.LEFT_ALIGNMENT);

                max_score = component.score.getAsInt();

                JLabel l = new JLabel(component.question);
                l.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 20));
                JPanel button_panel = new JPanel(new GridLayout(component.options.size(), 1));
                for (int i = 0; i < component.options.size(); i++) {
                    CheckBox jCheckBox = new CheckBox(component.options.get(i).getFirst() + ".  " + component.options.get(i).getLast());
                    check_actions.add(ele -> {
                        if (ele.equals("check")) {
                            jCheckBox.setEnabled(false);
                        }
                    });
                    try {
                        for (int j = 0; j < component.answer.getAsJsonArray().size(); j++) {
                            if (i == component.answer.getAsJsonArray().get(j).getAsInt()) {
                                check_actions.add(ele -> {
                                    if (ele.equals("check")) {
                                        answer.add(jCheckBox.isSelected());
                                    }
                                });
                                break;
                            }
                        }
                        check_actions.add(ele -> {
                            if (ele.equals("check")) {
                                boolean k = true;
                                l.setForeground(Color.green);
                                score = component.score.getAsInt();
                                for (Boolean b : answer) {
                                    if (!b) {
                                        k = false;
                                        l.setForeground(Color.red);

                                        score = 0;
                                        break;
                                    }
                                }
                                answer.clear();
                                answer.add(k);
                            }
                        });
                        button_panel.add(jCheckBox);
                    } catch (UnsupportedOperationException _) {
                        JLabel t = new JLabel(config.getLang("error"));
                        t.setToolTipText(config.getLang("error"));
                        button_panel.add(t);
                        answerCount = 0;
                    }
                    jCheckBox.setFont(new Font(config.getString("settings.font"), Font.PLAIN, jCheckBox.getFont().getSize()));
                }
                add(button_panel);
                add(l, BorderLayout.NORTH);
                break;
            }
            case "true_or_false":{
                answerCount++;
                setLayout(new BorderLayout());
                setAlignmentX(Component.LEFT_ALIGNMENT);
                max_score = component.score.getAsInt();

                JLabel l = new JLabel(component.question);
                l.setFont(new Font(config.getString("settings.font"), Font.PLAIN,20));
                JPanel button_panel = new JPanel(new GridLayout(1,2));
                RadioGroup buttonGroup = new RadioGroup();

                RadioButton correct = new RadioButton(config.getLang("correct"),buttonGroup);
                RadioButton incorrect = new RadioButton(config.getLang("incorrect"),buttonGroup);

                buttonGroup.add(correct);
                buttonGroup.add(incorrect);
                button_panel.add(correct);
                button_panel.add(incorrect);

                check_actions.add(ele->{
                    if (ele.equals("check")) {
                        if ((component.answer.getAsBoolean() == correct.isSelected()) && ((!component.answer.getAsBoolean()) == incorrect.isSelected())) {
                            answer.add(true);
                            l.setForeground(Color.green);
                            score = component.score.getAsInt();

                        } else {
                            answer.add(false);
                            l.setForeground(Color.red);
                        }

                        correct.setEnabled(false);
                        incorrect.setEnabled(false);
                    }
                });

                incorrect.setFont(new Font(config.getString("settings.font"), Font.PLAIN,incorrect.getFont().getSize()));
                correct.setFont(new Font(config.getString("settings.font"), Font.PLAIN,correct.getFont().getSize()));

                add(button_panel);
                add(l,BorderLayout.NORTH);
                break;
            }
            case "essay":
                break;
        }
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public ArrayList<Boolean> getAnswer() {
        return answer;
    }
}
