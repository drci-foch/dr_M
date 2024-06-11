package org.foch.application.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.foch.application.data.InMemoryData;
import org.foch.application.model.GridData;
import org.foch.application.views.accueil.AccueilView;

import java.util.List;

public class GridTable extends Div {

    public Component view;

    public GridTable() {
        UI.getCurrent().getPage().addStyleSheet(InMemoryData.serverUrl + "/w3.css");
        UI.getCurrent().getPage().addJavaScript(InMemoryData.serverUrl + "/w3.js");
        UI.getCurrent().getPage().addJavaScript(InMemoryData.serverUrl + "/gridtable.js");
        UI.getCurrent().getPage().addStyleSheet(InMemoryData.serverUrl + "/deco.css");
        setId("gridTable");
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        runBeforeClientResponse(ui -> ui.getPage().executeJs(
                "window.Vaadin.gridtable.initLazy($0)", getElement()));
    }

    public void fillData(List<GridData> gridDataList) {
        JsonArray rows = Json.createArray();
        int i=0;
        for (var data : gridDataList) {
            JsonObject jo = Json.createObject();
            jo.put("idDoc", data.idDoc);
            jo.put("sex", data.sex);
            jo.put("number", data.number);
            jo.put("ipp", data.ipp);
            jo.put("ddn", data.ddn);
            jo.put("titre", data.titre);
            jo.put("date", data.date);
            jo.put("contexte", data.contexte);
            rows.set(i, jo);
            i++;
        }
        getElement().callJsFunction("$connector.updateTable", rows);
    }

    @ClientCallable
    public void updateChanges(JsonArray changes) {

    }

    @ClientCallable
    public void getDoc(String idDoc){
        ((AccueilView) view).displayDoc(idDoc);
    }

}
