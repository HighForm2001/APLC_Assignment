package operation.gui_operation;

import operation.csv_operation.CsvOperation;
import operation.prolog_operation.PrologOperation;

import javax.swing.*;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class HomePage extends JFrame{
    private static final Map<String,Integer> country_total_confirmed = CsvOperation.get_total_confirmed();
    private static final Map<String,Integer> country_total_deaths = CsvOperation.get_total_deaths();
    private static final Map<String,Integer> country_total_recovered = CsvOperation.get_total_recovered();
    private JPanel MainPanel;
    private JButton showRecordButton;
    private JRadioButton dailyCaseRadioButton;
    private JRadioButton weekCaseRadioButton;
    private JRadioButton monthCaseRadioButton;
    private JRadioButton showAllRadioButton;
    private JRadioButton findHighestRadioButton;
    private JRadioButton findLowestRadioButton;
    private JRadioButton deathCaseRadioButton;
    private JRadioButton confirmedCaseRadioButton;
    private JRadioButton recoveredCaseRadioButton;
    private JLabel caseLabel;
    private JLabel timeLabel;
    private JLabel showLabel;
    private JPanel timePanel;
    private JPanel firstTab;
    private JPanel secondTab;
    private JTabbedPane TabbedPane;
    private JComboBox countryBox;
    private JLabel countryLabel;
    private JTextPane textPane1;
    private JScrollPane scrollPane1;
    private JLabel orderLabel;
    private JRadioButton ascendingOrderRadioButton;
    private JRadioButton descendingOrderRadioButton;
    private JLabel caseLabel2;
    private JRadioButton confirmedCaseRadioButton1;
    private JRadioButton deathCaseRadioButton1;
    private JRadioButton recoveredCaseRadioButton1;
    private JButton showButton;
    private JTextPane prologPane;
    private JRadioButton allTimeRadioButton;
    private ButtonGroup buttonGroup1;
    private ButtonGroup OrderGroup;
    private ButtonGroup CaseType2;
    private ButtonGroup ShowType;
    private ButtonGroup CaseType;
    private ButtonGroup TimeGroup;

    public HomePage() {
        setContentPane(MainPanel);
        setTitle("Covid19 Cases");
        setSize(500, 700);
        setLocationRelativeTo(null);
        allTimeRadioButton.setVisible(false);

        addCountry();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        TabbedPane.setTitleAt(0, "Csv Operation");
        TabbedPane.setTitleAt(1, "Prolog Operation");
        addShowRecordButtonListener();
        addShowButtonListener();
        addRadioButtonListener();
        setVisible(true);

    }

    private void addRadioButtonListener() {
        findLowestRadioButton.addActionListener(e -> {
            allTimeRadioButton.setVisible(false);
            buttonGroup1.clearSelection();
        });
        findHighestRadioButton.addActionListener(e -> {
            allTimeRadioButton.setVisible(false);
            buttonGroup1.clearSelection();
        });
        showAllRadioButton.addActionListener(e -> allTimeRadioButton.setVisible(true));
        allTimeRadioButton.addActionListener(e -> TimeGroup.clearSelection());
        monthCaseRadioButton.addActionListener(e -> buttonGroup1.clearSelection());
        weekCaseRadioButton.addActionListener(e -> buttonGroup1.clearSelection());
        dailyCaseRadioButton.addActionListener(e -> buttonGroup1.clearSelection());
    }

    private void addShowButtonListener() {
        showButton.addActionListener(e -> {
            if(CaseType2.getSelection()==null || OrderGroup.getSelection()==null)
                JOptionPane.showMessageDialog(null,"Please select all the options in this tab to show records!");
            else{
                String order = getSelectedButton(OrderGroup);
                String caseType = getSelectedButton(CaseType2);
                Map<String,Integer> to_display;
                if(caseType.equals("Confirmed Case"))
                    to_display = country_total_confirmed;
                else if(caseType.equals("Death Case"))
                    to_display = country_total_deaths;
                else
                    to_display = country_total_recovered;
                PrologOperation po = new PrologOperation();
                if(po.isConnected()) {

                    po.show_sorted_list(to_display, caseType, prologPane, order);
//                    else
//                        po.show_sorted_list_descending(to_display,caseType,prologPane);
                }
                else
                    JOptionPane.showMessageDialog(null,"Connection failed.");
            }
        });
    }
    private void addShowRecordButtonListener() {
        showRecordButton.addActionListener(e -> {
            List<List<String>> to_process;
            if(ShowType.getSelection() == null || CaseType.getSelection() == null ||
                    (TimeGroup.getSelection() == null&&buttonGroup1.getSelection()==null))
                JOptionPane.showMessageDialog(null,"Please select an option from each preference to proceed!");
            else {
                String caseType = getSelectedButton(CaseType);
                String showType = getSelectedButton(ShowType);
                String timeType = getSelectedButton(TimeGroup)==null?getSelectedButton(buttonGroup1):getSelectedButton(TimeGroup);
                String selected_country = countryBox.getSelectedItem().toString();
                if(caseType.equals("Confirmed Case"))
                    to_process = CsvOperation.getConfirmed_list();
                else if(caseType.equals("Death Case"))
                    to_process = CsvOperation.getDeaths_list();
                else
                    to_process = CsvOperation.getRecovered_list();
                Map to_display = switch (timeType) {
                    case "Daily Sum" -> CsvOperation.compute_daily(to_process);
                    case "Week Sum" -> CsvOperation.compute_weekly(to_process);
                    case "Month Sum" -> CsvOperation.compute_monthly(to_process);
                    default -> CsvOperation.total_cases(to_process);
                };
                if(showType.equals("Find Highest"))
                    CsvOperation.find_highest(to_display, textPane1,timeType,caseType,selected_country);
                else if(showType.equals("Find Lowest"))
                    CsvOperation.find_lowest(to_display,textPane1,timeType,caseType,selected_country);
                else {
                    textPane1.setText(caseType + " in " + timeType + "\n");
                    if(timeType.equals("All Time"))
                        CsvOperation.show_result_all(to_display, textPane1, selected_country);
                    else
                        CsvOperation.show_result_time(to_display,textPane1,selected_country);
                }
            }
        });
    }
    private void addCountry(){
        countryBox.addItem("All country");
        country_total_confirmed.forEach((key, value) -> countryBox.addItem(key));
    }
    private String getSelectedButton(ButtonGroup bg){
        Enumeration<AbstractButton> element = bg.getElements();
        while(element.hasMoreElements()){
            AbstractButton button = element.nextElement();
            if(button.isSelected())
                return button.getText();
        }
        return null;
    }
}
