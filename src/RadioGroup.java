import java.util.ArrayList;

public class RadioGroup {
    private ArrayList<RadioButton> radioButtons = new ArrayList<>();

    public ArrayList<RadioButton> getRadioButtons() {
        return radioButtons;
    }

    public void add(RadioButton b) {
        radioButtons.add(b);
    }


    public void remove(RadioButton b) {
        radioButtons.remove(b);
    }
}
