window.Vaadin.gridtable={
    initLazy: function (c) {
        if (c.$connector) {
            return;
        }
        c.$connector = {};

        const pushChanges = function(value) {
            c.$server.updateChanges(value);
        }

        var table = document.createElement('table');
        table.id = 'myTable';
        var strTable = '<table"></table>';
        table.innerHTML=strTable;
        table.classList.add("w3-table-all", "w3-hoverable", "w3-tiny");
        table.style.width="1000px";

        var containerTable = document.getElementById("gridTable");
        containerTable.style.height="800px";
        containerTable.style.overflowY = "scroll";
        containerTable.appendChild(table);

        table.addEventListener('click', function(e) {
            e = e || window.event;
            var target = e.target || e.srcElement;
            if (target.tagName && target.tagName==="TD"){
                var idDoc = target.parentNode.firstChild.id;
                c.$server.getDoc(idDoc);
                var rows = table.getElementsByTagName('tr');
                for (var i = 0; i < rows.length; i++) {
                    rows[i].classList.remove('highlight');
                }
                var clickedRow = e.target.parentNode;
                clickedRow.classList.add('highlight');
            }
        });
        c.$connector.updateTable = function(lines){
            var table = document.getElementById('myTable');
            var tbody = table.querySelector('tbody');

            // Clear existing tbody
            if (tbody) {
                tbody.parentNode.removeChild(tbody);
            }

            tbody = document.createElement('tbody');
            var tr = document.createElement('tr');
            tr.classList.add("stick");
            tr.innerHTML = '<th>N°</th><th>IPP</th><th>Sexe</th><th>DDN</th><th>Date</th><th>Titre</th><th style="width:500px">Contexte</th>';
            tbody.appendChild(tr);
            for (var i = 0; i < lines.length; i++) {
                var row = lines[i];
                var tr = document.createElement('tr');

                tr.innerHTML = '<td id="'+row.idDoc +'">' + row.number + '</td>' +
                    '<td>' + row.ipp + '</td>' +
                    '<td>' + row.sex + '</td>' +
                    '<td>' + row.ddn + '</td>' +
                    '<td>' + row.date + '</td>' +
                    '<td>' + row.titre + '</td>' +
                    '<td>' + row.contexte + '</td>';

                tbody.appendChild(tr);
            }

            table.appendChild(tbody);
        }
    }

}