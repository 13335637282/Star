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
        log.AddLog(Log.TRACE, "[QuestionComponent] getScore called - returning: " + score);
        return score;
    }

    public int getMax_score() {
        log.AddLog(Log.TRACE, "[QuestionComponent] getMax_score called - returning: " + max_score);
        return max_score;
    }

    public void check() {
        log.AddLog(Log.INFO, "[QuestionComponent] check() called - questionType: " + questionType +
                ", answerCount: " + answerCount + ", check_actions size: " + check_actions.size());

        long startTime = System.nanoTime();
        answer = new ArrayList<>();

        log.AddLog(Log.DEBUG, "[QuestionComponent] Executing " + check_actions.size() + " check actions");
        for (int i = 0; i < check_actions.size(); i++) {
            Consumer<? super String> action = check_actions.get(i);
            try {
                log.AddLog(Log.TRACE, "[QuestionComponent] Executing check action " + (i + 1) + "/" + check_actions.size());
                action.accept("check");
            } catch (Exception e) {
                log.AddERRORLog("[QuestionComponent] Error executing check action " + (i + 1), e);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000; // 转换为微秒
        log.AddLog(Log.INFO, "[QuestionComponent] check() completed - final answer count: " +
                answer.size() + ", score: " + score + "/" + max_score +
                ", duration: " + duration + " microseconds");
    }

    public QuestionComponent(StarUI.PaperComponent component) {
        log.AddLog(Log.INFO, "[QuestionComponent] Creating new component - type: " +
                component.type + ", question: " +
                (component.question != null ? component.question.substring(0, Math.min(50, component.question.length())) + "..." : "null"));

        long constructorStartTime = System.nanoTime();
        questionType = component.type;

        try {
            switch (component.type) {
                case "fill_in_the_blank": {
                    log.AddLog(Log.INFO, "[QuestionComponent] Processing fill_in_the_blank question");
                    setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
                    setAlignmentX(Component.LEFT_ALIGNMENT);

                    log.AddLog(Log.DEBUG, "[QuestionComponent] Processing " + component.value.size() + " value elements");
                    for (int i = 0; i < component.value.size(); i++) {
                        JsonElement element = component.value.get(i);
                        try {
                            String textValue = element.getAsString();
                            log.AddLog(Log.DEBUG, "[QuestionComponent] Adding text label: '" +
                                    textValue.substring(0, Math.min(30, textValue.length())) + "...'");

                            JLabel text = new JLabel(textValue);
                            text.setFont(new Font(config.getString("settings.font"), Font.PLAIN, text.getFont().getSize()));
                            add(text);
                        } catch (UnsupportedOperationException e) {
                            log.AddLog(Log.DEBUG, "[QuestionComponent] Element " + i + " is a blank field");

                            int maxChars = element.getAsJsonObject().get("maximum_chars").getAsInt() <= 0 ?
                                    20 : element.getAsJsonObject().get("maximum_chars").getAsInt();
                            log.AddLog(Log.DEBUG, "[QuestionComponent] Creating text field with maxChars: " + maxChars);

                            JTextField field = new JTextField(maxChars);
                            field.setFont(new Font(config.getString("settings.font"), Font.PLAIN, field.getFont().getSize()));

                            Color originalColor = field.getForeground();
                            answerCount++;

                            log.AddLog(Log.DEBUG, "[QuestionComponent] Setting field size for maxChars: " + maxChars);
                            field.setSize(new Dimension(maxChars * 24, 25));
                            field.setMaximumSize(new Dimension(maxChars * 24, 25));

                            try {
                                String correctAnswer = element.getAsJsonObject().get("answer").getAsString();
                                int questionScore = element.getAsJsonObject().get("score").getAsInt();

                                log.AddLog(Log.DEBUG, "[QuestionComponent] Setting up check action for field - correctAnswer: '" +
                                        correctAnswer + "', score: " + questionScore);

                                check_actions.add(ele -> {
                                    if (ele.equals("check")) {
                                        log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Checking fill_in_the_blank field");
                                        String userAnswer = field.getText();
                                        boolean isCorrect = userAnswer.equals(correctAnswer);

                                        log.AddLog(Log.INFO, "[QuestionComponent/Check] Fill in blank result - " +
                                                "user: '" + userAnswer + "', correct: '" + correctAnswer +
                                                "', isCorrect: " + isCorrect);

                                        if (isCorrect) {
                                            answer.add(true);
                                            field.setBackground(Color.green);
                                            score += questionScore;
                                            log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Answer correct, added score: " + questionScore);
                                        } else {
                                            answer.add(false);
                                            field.setBackground(Color.red);
                                            log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Answer incorrect");
                                        }
                                        field.setEnabled(false);
                                    }
                                });
                                max_score += questionScore;
                                log.AddLog(Log.DEBUG, "[QuestionComponent] Max score updated to: " + max_score);

                            } catch (UnsupportedOperationException e2) {
                                answerCount--;
                                log.AddLog(Log.ERROR, "[QuestionComponent] Error processing blank field - missing answer or score");
                                field.setEnabled(false);
                                String errorText = config.getLang("error_blank");
                                field.setText(errorText);
                                field.setToolTipText(errorText);
                                field.setSize(new Dimension(errorText.length() * 24, 25));
                                field.setMinimumSize(new Dimension(errorText.length() * 24, 25));

                                // 注释掉的AI评分代码保留原样
                                log.AddLog(Log.DEBUG, "[QuestionComponent] AI scoring code is commented out");
                            }

                            log.AddLog(Log.DEBUG, "[QuestionComponent] Adding field to panel and setting up key listener");
                            add(field);

                            field.addKeyListener(new KeyListener() {
                                @Override
                                public void keyTyped(KeyEvent e) {
                                    log.AddLog(Log.TRACE, "[QuestionComponent/KeyListener] keyTyped - char: '" + e.getKeyChar() + "'");
                                }

                                @Override
                                public void keyPressed(KeyEvent e) {
                                    log.AddLog(Log.TRACE, "[QuestionComponent/KeyListener] keyPressed - keyCode: " + e.getKeyCode());
                                    if (field.getText().length() > maxChars) {
                                        log.AddLog(Log.DEBUG, "[QuestionComponent/KeyListener] Text exceeds max length, truncating");
                                        field.setText(field.getText().substring(0, maxChars));
                                        new Thread(() -> {
                                            field.setForeground(Color.red);
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException ie) {
                                                log.AddLog(Log.DEBUG, "[QuestionComponent/KeyListener] Color reset thread interrupted");
                                                Thread.currentThread().interrupt();
                                                field.setForeground(originalColor);
                                            }
                                            field.setForeground(originalColor);
                                            log.AddLog(Log.DEBUG, "[QuestionComponent/KeyListener] Field color reset to normal");
                                        }).start();
                                    }
                                }

                                @Override
                                public void keyReleased(KeyEvent e) {
                                    log.AddLog(Log.TRACE, "[QuestionComponent/KeyListener] keyReleased - keyCode: " + e.getKeyCode());
                                    if (field.getText().length() > maxChars) {
                                        log.AddLog(Log.DEBUG, "[QuestionComponent/KeyListener] Text exceeds max length on release, truncating");
                                        field.setText(field.getText().substring(0, maxChars));
                                        new Thread(() -> {
                                            field.setForeground(Color.red);
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException ie) {
                                                log.AddLog(Log.DEBUG, "[QuestionComponent/KeyListener] Color reset thread interrupted");
                                                Thread.currentThread().interrupt();
                                                field.setForeground(originalColor);
                                            }
                                            field.setForeground(originalColor);
                                        }).start();
                                    }
                                }
                            });
                        }
                    }
                    break;
                }
                case "application": {
                    log.AddLog(Log.INFO, "[QuestionComponent] Processing application question");
                    setLayout(new BorderLayout());
                    setAlignmentX(Component.LEFT_ALIGNMENT);

                    JLabel label = new JLabel(component.question);
                    label.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 20));
                    add(label, BorderLayout.NORTH);
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Added question label: '" +
                            component.question.substring(0, Math.min(50, component.question.length())) + "...'");

                    JPanel center = new JPanel(new GridLayout(2, 1));

                    JTextArea area = new JTextArea();
                    area.setFont(new Font(config.getString("settings.font"), Font.PLAIN, area.getFont().getSize()));
                    center.add(area);
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Added text area");

                    JScrollPane scrollPane = new JScrollPane();
                    JPanel input_pane = new JPanel();
                    int answerCount = component.answer.getAsJsonArray().size();
                    input_pane.setLayout(new GridLayout(answerCount, 2));
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Setting up input panel for " + answerCount + " answers");

                    for (int i = 0; i < answerCount; i++) {
                        this.answerCount++;
                        JLabel text = new JLabel(config.getLang("question_number").formatted(i + 1));
                        text.setFont(new Font(config.getString("settings.font"), Font.PLAIN, text.getFont().getSize()));

                        JTextField field = new JTextField();
                        field.setFont(new Font(config.getString("settings.font"), Font.PLAIN, field.getFont().getSize()));

                        int questionScore = component.score.getAsJsonArray().get(i).getAsInt();
                        max_score += questionScore;
                        log.AddLog(Log.DEBUG, "[QuestionComponent] Added answer field " + (i + 1) + " with score: " + questionScore);

                        int finalI = i;
                        check_actions.add(ele -> {
                            if (ele.equals("check")) {
                                log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Checking application question field " + (finalI + 1));
                                String correctAnswer = component.answer.getAsJsonArray().get(finalI).getAsString();
                                String userAnswer = field.getText();
                                boolean isCorrect = userAnswer.equals(correctAnswer);

                                log.AddLog(Log.INFO, "[QuestionComponent/Check] Application question result - " +
                                        "field " + (finalI + 1) + ", user: '" + userAnswer +
                                        "', correct: '" + correctAnswer + "', isCorrect: " + isCorrect);

                                if (isCorrect) {
                                    answer.add(true);
                                    field.setBackground(Color.green);
                                    score += component.score.getAsJsonArray().get(finalI).getAsInt();
                                    log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Answer correct, added score: " +
                                            component.score.getAsJsonArray().get(finalI).getAsInt());
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
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Application question setup complete");
                    break;
                }
                case "single_choice": {
                    log.AddLog(Log.INFO, "[QuestionComponent] Processing single_choice question");
                    answerCount++;
                    setLayout(new BorderLayout());
                    setAlignmentX(Component.LEFT_ALIGNMENT);
                    max_score = component.score.getAsInt();
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Single choice max score: " + max_score);

                    JLabel questionLabel = new JLabel(component.question);
                    questionLabel.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 20));
                    JPanel button_panel = new JPanel(new GridLayout(component.options.size(), 1));
                    RadioGroup buttonGroup = new RadioGroup();
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Setting up " + component.options.size() + " options");

                    int correctAnswerIndex = component.answer.getAsInt();
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Correct answer index: " + correctAnswerIndex);

                    for (int i = 0; i < component.options.size(); i++) {
                        String optionText = component.options.get(i).getFirst() + ".  " + component.options.get(i).getLast();
                        RadioButton radioButton = new RadioButton(optionText, buttonGroup);
                        log.AddLog(Log.DEBUG, "[QuestionComponent] Created radio button " + i + ": '" +
                                optionText.substring(0, Math.min(30, optionText.length())) + "...'");

                        try {
                            if (i == correctAnswerIndex) {
                                log.AddLog(Log.DEBUG, "[QuestionComponent] Setting up check action for correct option " + i);
                                check_actions.add(ele -> {
                                    if (ele.equals("check")) {
                                        log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Checking single choice answer");
                                        boolean isSelected = radioButton.isSelected();
                                        answer.add(isSelected);

                                        log.AddLog(Log.INFO, "[QuestionComponent/Check] Single choice result - " +
                                                "selected: " + isSelected + ", correct index: " + correctAnswerIndex);

                                        if (isSelected) {
                                            questionLabel.setForeground(Color.green);
                                            score = component.score.getAsInt();
                                            log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Correct answer selected, score: " + score);
                                        } else {
                                            questionLabel.setForeground(Color.red);
                                            log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Wrong answer selected");
                                        }
                                    }
                                    radioButton.setEnabled(false);
                                });
                            } else {
                                check_actions.add(ele -> {
                                    if (ele.equals("check")) {
                                        radioButton.setEnabled(false);
                                    }
                                });
                            }

                            buttonGroup.add(radioButton);
                            button_panel.add(radioButton);
                        } catch (UnsupportedOperationException e) {
                            log.AddLog(Log.ERROR, "[QuestionComponent] Error processing single choice option " + i);
                            JLabel errorLabel = new JLabel(config.getLang("error"));
                            errorLabel.setToolTipText(config.getLang("error"));
                            button_panel.add(errorLabel);
                            answerCount = 0;
                        }
                        radioButton.setFont(new Font(config.getString("settings.font"), Font.PLAIN, radioButton.getFont().getSize()));
                    }
                    add(button_panel);
                    add(questionLabel, BorderLayout.NORTH);
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Single choice question setup complete");
                    break;
                }
                case "multiple_choice": {
                    log.AddLog(Log.INFO, "[QuestionComponent] Processing multiple_choice question");
                    answerCount++;
                    setLayout(new BorderLayout());
                    setAlignmentX(Component.LEFT_ALIGNMENT);

                    max_score = component.score.getAsInt();
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Multiple choice max score: " + max_score);

                    JLabel questionLabel = new JLabel(component.question);
                    questionLabel.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 20));
                    JPanel button_panel = new JPanel(new GridLayout(component.options.size(), 1));
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Setting up " + component.options.size() + " options");

                    // 记录正确答案索引
                    ArrayList<Integer> correctAnswers = new ArrayList<>();
                    for (int j = 0; j < component.answer.getAsJsonArray().size(); j++) {
                        correctAnswers.add(component.answer.getAsJsonArray().get(j).getAsInt());
                    }
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Correct answer indices: " + correctAnswers);

                    for (int i = 0; i < component.options.size(); i++) {
                        String optionText = component.options.get(i).getFirst() + ".  " + component.options.get(i).getLast();
                        CheckBox checkBox = new CheckBox(optionText);
                        log.AddLog(Log.DEBUG, "[QuestionComponent] Created checkbox " + i + ": '" +
                                optionText.substring(0, Math.min(30, optionText.length())) + "...'");

                        // 添加禁用操作
                        check_actions.add(ele -> {
                            if (ele.equals("check")) {
                                checkBox.setEnabled(false);
                            }
                        });

                        try {
                            // 为正确答案设置检查逻辑
                            if (correctAnswers.contains(i)) {
                                log.AddLog(Log.DEBUG, "[QuestionComponent] Setting up check action for correct option " + i);
                                int finalI = i;
                                check_actions.add(ele -> {
                                    if (ele.equals("check")) {
                                        answer.add(checkBox.isSelected());
                                        log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Multiple choice option " + finalI +
                                                " selected: " + checkBox.isSelected());
                                    }
                                });
                            }

                            // 添加最终检查逻辑
                            check_actions.add(ele -> {
                                if (ele.equals("check")) {
                                    log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Final multiple choice check");
                                    boolean allCorrect = true;
                                    questionLabel.setForeground(Color.green);
                                    score = component.score.getAsInt();

                                    for (Boolean b : answer) {
                                        if (!b) {
                                            allCorrect = false;
                                            questionLabel.setForeground(Color.red);
                                            score = 0;
                                            break;
                                        }
                                    }

                                    log.AddLog(Log.INFO, "[QuestionComponent/Check] Multiple choice final result - " +
                                            "allCorrect: " + allCorrect + ", score: " + score);

                                    answer.clear();
                                    answer.add(allCorrect);
                                }
                            });

                            button_panel.add(checkBox);
                        } catch (UnsupportedOperationException e) {
                            log.AddLog(Log.ERROR, "[QuestionComponent] Error processing multiple choice option " + i);
                            JLabel errorLabel = new JLabel(config.getLang("error"));
                            errorLabel.setToolTipText(config.getLang("error"));
                            button_panel.add(errorLabel);
                            answerCount = 0;
                        }
                        checkBox.setFont(new Font(config.getString("settings.font"), Font.PLAIN, checkBox.getFont().getSize()));
                    }
                    add(button_panel);
                    add(questionLabel, BorderLayout.NORTH);
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Multiple choice question setup complete");
                    break;
                }
                case "true_or_false": {
                    log.AddLog(Log.INFO, "[QuestionComponent] Processing true_or_false question");
                    answerCount++;
                    setLayout(new BorderLayout());
                    setAlignmentX(Component.LEFT_ALIGNMENT);
                    max_score = component.score.getAsInt();
                    log.AddLog(Log.DEBUG, "[QuestionComponent] True/False max score: " + max_score);

                    JLabel questionLabel = new JLabel(component.question);
                    questionLabel.setFont(new Font(config.getString("settings.font"), Font.PLAIN, 20));
                    JPanel button_panel = new JPanel(new GridLayout(1, 2));
                    RadioGroup buttonGroup = new RadioGroup();

                    boolean correctAnswer = component.answer.getAsBoolean();
                    log.AddLog(Log.DEBUG, "[QuestionComponent] Correct answer: " + correctAnswer);

                    RadioButton correctButton = new RadioButton(config.getLang("correct"), buttonGroup);
                    RadioButton incorrectButton = new RadioButton(config.getLang("incorrect"), buttonGroup);

                    buttonGroup.add(correctButton);
                    buttonGroup.add(incorrectButton);
                    button_panel.add(correctButton);
                    button_panel.add(incorrectButton);

                    check_actions.add(ele -> {
                        if (ele.equals("check")) {
                            log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Checking true/false answer");
                            boolean userCorrectSelected = correctButton.isSelected();
                            boolean userIncorrectSelected = incorrectButton.isSelected();
                            boolean isCorrect = (correctAnswer == userCorrectSelected) && (!correctAnswer == userIncorrectSelected);

                            log.AddLog(Log.INFO, "[QuestionComponent/Check] True/False result - " +
                                    "correctAnswer: " + correctAnswer + ", userSelectedCorrect: " + userCorrectSelected +
                                    ", userSelectedIncorrect: " + userIncorrectSelected + ", isCorrect: " + isCorrect);

                            if (isCorrect) {
                                answer.add(true);
                                questionLabel.setForeground(Color.green);
                                score = component.score.getAsInt();
                                log.AddLog(Log.DEBUG, "[QuestionComponent/Check] Answer correct, score: " + score);
                            } else {
                                answer.add(false);
                                questionLabel.setForeground(Color.red);
                            }

                            correctButton.setEnabled(false);
                            incorrectButton.setEnabled(false);
                        }
                    });

                    incorrectButton.setFont(new Font(config.getString("settings.font"), Font.PLAIN, incorrectButton.getFont().getSize()));
                    correctButton.setFont(new Font(config.getString("settings.font"), Font.PLAIN, correctButton.getFont().getSize()));

                    add(button_panel);
                    add(questionLabel, BorderLayout.NORTH);
                    log.AddLog(Log.DEBUG, "[QuestionComponent] True/False question setup complete");
                    break;
                }
                case "essay": {
                    log.AddLog(Log.INFO, "[QuestionComponent] Processing essay question (not implemented)");
                    // Essay question implementation would go here
                    break;
                }
                default: {
                    log.AddLog(Log.WARNING, "[QuestionComponent] Unknown question type: " + component.type);
                    break;
                }
            }

            long constructorEndTime = System.nanoTime();
            long constructorDuration = (constructorEndTime - constructorStartTime) / 1000;
            log.AddLog(Log.INFO, "[QuestionComponent] Constructor completed - type: " + questionType +
                    ", answerCount: " + answerCount + ", max_score: " + max_score +
                    ", duration: " + constructorDuration + " microseconds");

        } catch (Exception e) {
            log.AddERRORLog("[QuestionComponent] Error constructing question component - type: " + questionType, e);
        }
    }

    public int getAnswerCount() {
        log.AddLog(Log.TRACE, "[QuestionComponent] getAnswerCount called - returning: " + answerCount);
        return answerCount;
    }

    public ArrayList<Boolean> getAnswer() {
        log.AddLog(Log.TRACE, "[QuestionComponent] getAnswer called - returning " + answer.size() + " answers");
        return answer;
    }

    // 添加验证方法
    public boolean isValid() {
        log.AddLog(Log.DEBUG, "[QuestionComponent] Validating component - type: " + questionType);

        boolean valid = true;

        if (questionType == null || questionType.trim().isEmpty()) {
            log.AddLog(Log.ERROR, "[QuestionComponent] Invalid question type");
            valid = false;
        }

        if (answerCount < 0) {
            log.AddLog(Log.WARNING, "[QuestionComponent] Invalid answer count: " + answerCount);
        }

        if (check_actions == null) {
            log.AddLog(Log.ERROR, "[QuestionComponent] Check actions list is null");
            valid = false;
        }

        log.AddLog(Log.DEBUG, "[QuestionComponent] Validation result: " + (valid ? "VALID" : "INVALID"));
        return valid;
    }

    // 添加状态报告方法
    public void logState() {
        log.AddLog(Log.INFO, "[QuestionComponent/State] Type: " + questionType +
                ", AnswerCount: " + answerCount + ", Score: " + score +
                "/" + max_score + ", CheckActions: " + check_actions.size() +
                ", CurrentAnswers: " + answer.size());
    }

    @Override
    public String toString() {
        return "QuestionComponent{type='" + questionType + "', answerCount=" + answerCount +
                ", score=" + score + "/" + max_score + "}";
    }
}