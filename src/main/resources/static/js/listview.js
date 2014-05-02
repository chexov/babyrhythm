ListView = function(id, ui){
    this.container = document.getElementById(id);
    this.parentUI = ui;
}

ListView.prototype.createTable = function(days){
    var self = this;
    while (self.container.hasChildNodes()) {
        self.container.removeChild(self.container.lastChild);
    }
    var actionTable = document.createElement('table');
    actionTable.classList.add("table");
    actionTable.classList.add("table-striped");
    actionTable.classList.add("table-hover");
    actionTable.width = '720';
    actionTable.style.width = '720px';
    var rows = "<thead><tr><th>Action</th><th>Day</th><th>Time</th><th>&nbsp;</th></tr></thead><tbody>";

    var actions = [];
    for(var i in days){
        console.log(i);
        var timesObj = days[i];
        
        for (var j in timesObj)
            actions.push(timesObj[j]);
    }
    actions.sort(function(a,b){
        return b.timestamp - a.timestamp;
    });
    
    for (var i=0;i<actions.length;i++){
        var anAction = actions[i];
        var date = new Date();
        date.setTime(anAction.timestamp);
        var removeButton = "<button id=\""+anAction.timestamp+"\" class=\"deleteBabyAction btn btn-danger\">Remove</button>";
        rows += "<tr><td>"+anAction.type+"</td><td>"+date.getDate()+"/"+(date.getMonth()+1)+"/"+date.getFullYear()+"</td><td>"+date.getHours()+":"+date.getMinutes()+"</td><td>"+removeButton+"</td></tr>";
    }
    rows += "</tbody>";
    actionTable.innerHTML = rows;
    self.container.appendChild(actionTable);
    
    var buttons = document.getElementsByClassName("deleteBabyAction");
    for(var i=0;i<buttons.length;i++){
        var leButton = buttons[i];
        var leButtonId = buttons[i].id;
        leButton.addEventListener("click", function(evt){
            self.parentUI.remove(evt.target.id);
        });
        
    }
        
}