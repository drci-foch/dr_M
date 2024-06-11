package org.foch.application.views.accueil;

import com.github.appreciated.css.grid.GridLayoutComponent;
import com.github.appreciated.css.grid.sizes.Flex;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.MinMax;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.github.appreciated.layout.FlexibleGridLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import org.apache.commons.text.StringEscapeUtils;
import org.foch.application.components.GridTable;
import org.foch.application.data.InMemoryData;
import org.foch.application.data.SearchData;
import org.foch.application.data.service.ManticoreService;
import org.foch.application.model.DocContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PageTitle("Accueil")
@Route(value = "hello")
@RouteAlias(value = "")
public class AccueilView extends VerticalLayout {

    private FlexibleGridLayout pagingLayout = mosaicLayout("80px", "30px", false);
    private List<Div> pagingDivs = new ArrayList<>();
    private ManticoreService manticoreService;
    private Span nbResults = new Span();
    private Div docViz = new Div();
    private HorizontalLayout searchHeader;
    private SearchData currentSearch = new SearchData();
    private TextArea searchField = new TextArea("Moteur textuel");
    private TextField titre = new TextField("Titre");
    private ComboBox<String> sexField = new ComboBox<>("Sexe");
    private CheckboxGroup<String> originDoc = new CheckboxGroup<>();
    private IntegerField age1 = new IntegerField("Age >=");
    private IntegerField age2 = new IntegerField("Age <=");
    private TextField date1 = new TextField("Date doc >=");
    private TextField date2 = new TextField("Date doc <=");
    private VerticalLayout menu1, menu2, menu3;
    private Scroller menuOriginDocLayout;
    private static List<String> originCodeStrings = Arrays.asList("ARCH_INTERNE", "BIO", "CYBERLAB", "DOC_EXTERNE_Api", "DOC_EXTERNE_Ari", "DOC_EXTERNE_Car", "DOC_EXTERNE_CeS", "DOC_EXTERNE_COP", "DOC_EXTERNE_DIA", "DOC_EXTERNE_ECG", "DOC_EXTERNE_Med", "DOC_EXTERNE_None", "DOC_EXTERNE_Pat", "DOC_EXTERNE_PCA", "DOC_EXTERNE_Res", "DOC_EXTERNE_SOF", "DOC_EXTERNE_SPI", "DOC_EXTERNE_vie", "DOC_EXTERNE_XPl", "Easily", "Easily_Ari", "Easily_Car", "Easily_CeS", "Easily_COP", "Easily_DIA", "Easily_echo_cardio", "Easily_echo_gyneco", "Easily_Efitback", "Easily_EFR", "Easily_Med", "Easily_Muse", "Easily_Patientys", "Easily_PCA", "Easily_Res", "Easily_SOF", "Easily_Xpl", "FOCH_EFR", "PMSI_MCO");

    private static final Charset ENCODING = Charset.forName("Windows-1252");

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        runBeforeClientResponse(ui -> ui.getPage().executeJs(
                "window.Vaadin.search.initLazy($0)", getElement()));
    }

    public AccueilView(@Autowired ManticoreService manticoreService,
                       @Value("${export.path}") String exportPath) {
        UI.getCurrent().getPage().addJavaScript(InMemoryData.serverUrl + "/search.js");
        this.manticoreService = manticoreService;
        setMargin(false);
        setSpacing(false);
        searchField.setWidth("800px");
        HorizontalLayout h = new HorizontalLayout();
        Button soumettre = new Button("Soumettre");
        soumettre.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button reinit = new Button("Réinitialiser");
        searchHeader = new HorizontalLayout(searchField);
        originDoc.setValue(new HashSet<>());
        originDoc.setItems(originCodeStrings);
        //		originDoc.setHeight("160px");
        originDoc.setWidth("200px");
        Span sourceSpan = new Span("Source");
        AtomicBoolean selectAll = new AtomicBoolean(true);
        sourceSpan.addClickListener(e -> {
            selectAll.set(!selectAll.get());
            if (selectAll.get())
                originDoc.deselectAll();
            else
                originDoc.setValue(new HashSet<String>(originCodeStrings));
        });
        menuOriginDocLayout = new Scroller(new VerticalLayout(sourceSpan, originDoc));
        menuOriginDocLayout.setWidth("250px");
        menuOriginDocLayout.setHeight("180px");
        sexField.setItems(Arrays.asList("", "M", "F"));
        sexField.setValue("");
        var buttons = menuItemSearch(200, soumettre, reinit);
        buttons.getStyle().set("margin-top", "25px");
        menu1 = menuItemSearch(200, titre, sexField);
        menu2 = menuItemSearch(100, age1, age2);
        menu3 = menuItemSearch(130, date1, date2);
        showMenu(false);
        Icon icon1 = VaadinIcon.EYE.create();
        Icon icon2 = VaadinIcon.EYE_SLASH.create();
        Button showHideButton = new Button(icon1);
        showHideButton.getStyle().set("margin-top", "30px");
        showHideButton.addClickListener(e -> {
            if (showHideButton.getIcon().equals(icon1)) {
                showMenu(true);
                showHideButton.setIcon(icon2);
            } else {
                showMenu(false);
                showHideButton.setIcon(icon1);
            }
        });
        searchHeader.add(showHideButton, menu1, menu2, menu3, menuOriginDocLayout, buttons);
        reinit.addClickListener(e -> {
            date1.clear();
            date2.clear();
            age1.clear();
            age2.clear();
            searchField.clear();
            sexField.clear();
            titre.clear();
            originDoc.setValue(new HashSet<>());
        });

        searchHeader.setWidthFull();
        add(searchHeader, new Hr(), pagingLayout);

        GridTable gridTable = new GridTable();
        gridTable.setVisible(false);
        gridTable.view = this;
        add(h);
        h.setHeight("900px");
        VerticalLayout v = new VerticalLayout();
        v.setSpacing(false);
        v.setPadding(false);
        v.setMargin(false);
        v.add(nbResults, pagingLayout, gridTable);
        h.add(v, docViz);
        int step = 500;
        nbResults.getStyle().set("font-weight", "bold").set("cursor", "pointer");
        nbResults.addClickListener(e -> {
            String folder = exportPath + "/" + UUID.randomUUID();
            new File(folder).mkdirs();
            String path = folder + "/result.csv";
            List<String> ids = new ArrayList<>();
            try (OutputStreamWriter writer =
                         new OutputStreamWriter(new FileOutputStream(path), ENCODING)) {
                int size = Integer.parseInt(nbResults.getText().split("doc")[0].trim());
                for (int i = 0; i < Math.min(size, 20000); i = i + step) {
                    var data = manticoreService.queryDocs(currentSearch, i);
                    for (var line : data) {
                        ids.add(line.idDoc);
                        writer.write(line + "\n");
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error writing to file: " + ex.getMessage());
            }

            new Thread(() -> {
                int totalSize = ids.size();
                int batchSize = 50;
                int numberOfBatches = (totalSize + batchSize - 1) / batchSize; // Calculate the number of batches

                for (int batch = 0; batch < numberOfBatches; batch++) {
                    int start = batch * batchSize;
                    int end = Math.min(start + batchSize, totalSize);

                    // Extract the current batch
                    List<String> currentBatch = ids.subList(start, end);

                    List<DocContent> docs = manticoreService.batchDocs(currentBatch);
                    for (var doc : docs) {
                        try (OutputStreamWriter writer =
                                     new OutputStreamWriter(new FileOutputStream(folder + "/" + doc.idDoc + ".html"), ENCODING)) {
                            writer.write(doc.docContent);
                        } catch (Exception ex) {
                            System.err.println("Error writing to file: " + ex.getMessage());
                        }
                    }
                }
            }).start();
        });
        v.setWidth("60%");
        h.setWidthFull();

        soumettre.addClickListener(e -> {
            gridTable.setVisible(true);
            nbResults.setText("");
            pagingLayout.getContent().removeAll();
            pagingDivs.clear();
            docViz.removeAll();
            if (searchField.getValue() == null || searchField.getValue().length() < 2) {
                Notification.show("Requête invalide", 3000, Notification.Position.MIDDLE);
                gridTable.fillData(new ArrayList<>());
                return;
            }
            String searchValue = searchField.getValue().replace("'", "\\'");
            currentSearch.freeText = searchValue;
            currentSearch.age1 = (age1.getValue() != null ? age1.getValue() + "" : "");
            currentSearch.age2 = (age2.getValue() != null ? age2.getValue() + "" : "");
            currentSearch.date1 = (date1.getValue() != null ? date1.getValue() : "");
            currentSearch.date2 = (date2.getValue() != null ? date2.getValue() : "");
            currentSearch.sex = (sexField.getValue() != null ? sexField.getValue() : "");
            currentSearch.origin = String.join(",", originDoc.getValue());
            currentSearch.freeText = searchValue;
            String countDocs = manticoreService.countDocs(currentSearch);
            nbResults.setText(countDocs);
            int size = Integer.parseInt(countDocs.split("doc")[0].trim());
            gridTable.fillData(manticoreService.queryDocs(currentSearch, 0));
            for (int i = 0; i < Math.min(size, 20000); i = i + step) {
                Div a = new Div();
                a.addClassName("button-container");
                a.getStyle().set("font-size", "10px").set("text-align", "center");
                int finalI = i;
                int finalMaxI = Math.min(size, finalI + step);
                a.setText((finalI + 1) + "-" + (finalMaxI));
                pagingLayout.withItems(a);
                pagingDivs.add(a);
                a.addClickListener(evDiv -> {
                    gridTable.fillData(manticoreService.queryDocs(currentSearch, finalI));
                });
            }
            for (Div div : pagingDivs) {
                div.addClickListener(eDiv -> {
                    pagingDivs.forEach(d -> d.getStyle().remove("background-color"));
                    div.getStyle().set("background-color", "#5993DF");
                });
            }
            if (pagingDivs.size() > 0) {
                pagingDivs.get(0).getStyle().set("background-color", "#5993DF");
            }
            pagingLayout.getStyle().remove("height");
        });
    }

    private void showMenu(boolean visible) {
        menu1.setVisible(visible);
        menu2.setVisible(visible);
        menu3.setVisible(visible);
        menuOriginDocLayout.setVisible(visible);
    }

    private VerticalLayout menuItemSearch(int width, Component... component) {
        VerticalLayout v = new VerticalLayout(component);
        Arrays.stream(component).forEach(c -> {
            ((HasSize) c).setWidthFull();
            ((HasSize) c).setMaxHeight("100px");
        });
        v.setSpacing(false);
        v.setMargin(false);
        v.setPadding(false);
        v.setMaxWidth(width + "px");
        return v;
    }

    private FlexibleGridLayout mosaicLayout(String minMaxSize, String rowSize, boolean spacing) {
        FlexibleGridLayout result = new FlexibleGridLayout()
                .withColumns(Repeat.RepeatMode.AUTO_FILL, new MinMax(new Length(minMaxSize), new Flex(1)))
                .withAutoRows(new Length(rowSize))
                //                .withItems(items)
                .withPadding(false).withSpacing(spacing).withAutoFlow(GridLayoutComponent.AutoFlow.ROW_DENSE)
                //                .withOverflow(GridLayoutComponent.Overflow.AUTO)
                ;
        result.setSizeFull();
        return result;
    }

    public void displayDoc(String idDoc) {
        docViz.removeAll();
        Span span = new Span();
        String contentDoc = manticoreService.getContentDoc(Long.parseLong(idDoc));
        contentDoc = convertHtmlString(contentDoc);
//		try {
//			Files.write(Paths.get("D:/test.txt"), contentDoc.getBytes("UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        long t1 = System.currentTimeMillis();
        List<String> wordStrings = splitSentence(currentSearch.freeText
                .replace("<", " ")
                .replace(">", " ")
                .replace("(", " ").replace(")", " ").replace("|", ""));
        String highlightedContent = highlightWords(contentDoc, wordStrings, "highlight2");
        System.out.println((System.currentTimeMillis() - t1) + " ms");
        span.getElement().setProperty("innerHTML", highlightedContent);
        searchHeader.scrollIntoView();
        docViz.add(span);
        getElement().callJsFunction("$connector.manageHighlight");
        docViz.getElement().addEventListener("click", e -> {
            getElement().callJsFunction("$connector.goNextHighlight");
        });
    }

    public void fillSearch(SearchData searchData) {
        if (searchData.freeText != null)
            searchField.setValue(searchData.freeText);
        if (searchData.sex != null)
            sexField.setValue(searchData.sex);
        if (searchData.age1 != null && searchData.age1.length() > 0)
            age1.setValue(Integer.parseInt(searchData.age1));
        if (searchData.age2 != null && searchData.age2.length() > 0)
            age2.setValue(Integer.parseInt(searchData.age2));
        if (searchData.date1 != null)
            date1.setValue(searchData.date1);
        if (searchData.date2 != null)
            date2.setValue(searchData.date2);
        if (searchData.origin != null)
            originDoc.setValue(new HashSet<>(Arrays.asList(searchData.origin.split(","))));
    }

    public static String convertHtmlString(String htmlContent) {
        return StringEscapeUtils.unescapeHtml4(htmlContent);
    }

    public static String highlightWords(String htmlContent, List<String> wordsToHighlight, String cssClass) {
        // Build regex pattern to match the words
        String regex = "\\b(" + buildRegexPattern(wordsToHighlight) + ")\\b";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        // Replace matched words with highlighted versions
        Matcher matcher = pattern.matcher(removeAccents(htmlContent));
        List<MatchPosition> matchPositions = new ArrayList<>();
        while (matcher.find()) {
            matchPositions.add(new MatchPosition(matcher.start(), matcher.end()));
        }
        StringBuilder result = new StringBuilder();
        int prevEnd = 0;
        for (MatchPosition matchPosition : matchPositions) {
            int start = matchPosition.start;
            int end = matchPosition.end;
            result.append(htmlContent.substring(prevEnd, start))
                    .append("<span class=\"").append(cssClass).append("\">")
                    .append(htmlContent.substring(start, end))
                    .append("</span>");
            prevEnd = end;
        }
        result.append(htmlContent.substring(prevEnd));
        return result.toString();
    }

    private static String buildRegexPattern(List<String> wordsToHighlight) {
        List<String> normalizedWords = new ArrayList<>();
        for (String word : wordsToHighlight) {
            normalizedWords.add(normalize(word));
        }
        System.out.println("buildregex :" + String.join("|", normalizedWords));
        return String.join("|", normalizedWords);
    }

//    private static String normalize(String word) {
//        // Remove special characters except '*', and convert to lowercase
//    	String normalized = removeAccents(word).replaceAll("[^\\p{L}'\\s*]", "");
//        normalized = normalized.replaceAll("[~/\\\\]", "").replace("*", "\\w*");
//        return normalized.toLowerCase();
//    }
//    
//    private static String removeAccents(String input) {
//        return Normalizer.normalize(input, Normalizer.Form.NFD)
//                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
//    }


    private static String normalize(String word) {
        // Remove special characters except '*', and convert to lowercase
        String normalized = removeAccents(word).replaceAll("[^\\p{L}'\\d\\s*]", "");
        normalized = normalized.replaceAll("[~/\\\\]", "").replace("*", "\\w*");
        return normalized.toLowerCase();
    }

    private static String removeAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private static List<String> splitSentence(String sentence) {
        List<String> words = new ArrayList<>();
        Pattern pattern;
        if (sentence.contains("~")) {
            pattern = Pattern.compile("\\S+");
        } else {
            pattern = Pattern.compile("\"[^\"]*\"|\\S+");
        }
        Matcher matcher = pattern.matcher(sentence);
        while (matcher.find()) {
            String wString = matcher.group().replaceAll("\"", "");
            if (!wString.startsWith("~")) words.add(wString); //TODO if special characters not works
        }
        words.forEach(w -> System.out.println(w));
        return words;
    }

    public static boolean isNumber(String str) {
        // Define the pattern to match a number
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        // Return true if the string matches the pattern, false otherwise
        return pattern.matcher(str).matches();
    }

    private static class MatchPosition {
        int start;
        int end;

        MatchPosition(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

}
