UI = function(name){
    var selfUI = this;
    this.name = name;
    this.offline = false;
    this.mirror = new LocalActionsMirror(name);
    this.store = new ActionStore(name);
    this.chartView = new ChartView("canvases");
    this.listView = new ListView("lists", this);
    
    
    this.uploadBabyAction = new XHR('POST', '/babyactions/'+name);
};

UI.prototype.activeView = "chartView";

UI.prototype.refresh = function(){
    if (this.activeView == "chartView"){
        this.chartView.drawAllDays(this.mirror.days);
    } else {
        this.listView.createTable(this.mirror.days);
    }
}

UI.prototype.toggleView = function(viewId){
    this.activeView = viewId;
    // If views are refreshed on updated - this could be avoided. Is it worth it? Time will tell.
    this.refresh();
    if ("chartView" == this.activeView){
        document.getElementById("listPane").classList.remove("active");
        document.getElementById("canvasesPane").classList.add("active");
    } else {
        document.getElementById("canvasesPane").classList.remove("active");        
        document.getElementById("listPane").classList.add("active");
    }
    
};

UI.prototype.uploadLocalActions = function (){
    var self = this;
    while(this.store.size() > 0){
        var anAction = this.store.pop();
        var jsonAction = JSON.stringify(anAction);
        console.log("uploading "+jsonAction);
        this.uploadBabyAction.send(jsonAction, function(){
            var request = this;
            if (request.readyState == 4) {
                if (request.status == 200){
                    var savedAction = JSON.parse(this.responseText);
                    self.mirror.addAction(savedAction);
                    self.mirror.persist();
                    console.log('Successfully uploaded action '+this.responseText);
                    humane.log("Successfully uploaded action '"+savedAction.type+"'");
                } else {
                    this.store.push(anAction);
                }
            }
        });
    }
};

UI.prototype.convertAndSetActionDate = function(date){
    var dateTime = new Date();
    dateTime.setDate(date.getUTCDate());
    dateTime.setMonth(date.getUTCMonth());
    dateTime.setFullYear(date.getUTCFullYear());
    dateTime.setHours(date.getUTCHours());
    dateTime.setMinutes(date.getUTCMinutes());
    dateTime.setSeconds(0);
    dateTime.setMilliseconds(0);
    this.actionDate = dateTime;
};

UI.prototype.action = function(actionType){
    var action = {"type" : actionType, "timestamp":(new Date()).getTime()};
    this.mirror.addAction(action);
    this.mirror.persist();
    this.refresh();
    this.sendAction(action);
};
    
UI.prototype.sendAction = function(action){
    var self = this;
    
    if (self.offline){
        self.store.push(action);
        humane.log("No connection to server, action saved to local storage for future upload.");
    } else {
        var jsonAction = JSON.stringify(action);
        self.uploadBabyAction.send(jsonAction,  function(data) {
            var request = this;
            if (request.readyState == 4) {
                if (request.status == 200){
                    console.log("baby action POST request succeded");
                    self.uploadLocalActions();
                } else if (request.status == 0){
                    self.store.push(action);
                    humane.log("No connection to server, action saved to local storage for future upload.");
                }
            }
        });
    }
};

UI.prototype.addAction = function(){
    var self = this;
    if (!self.actionDate){
        humane.log('Please pick a date/time for this action');
        return;
    }
    
    var s = document.getElementById("manualAction");
    if (!s || !s.value){
        humane.log('Please pick an action type');
        return;
    }
    
    var action = {"type" : s.value, "timestamp":self.actionDate.getTime()};
    this.mirror.addAction(action);
    this.mirror.persist();
    this.refresh();
    
    this.sendAction(action);
};

UI.prototype.remove = function(timestampStr){
    var self = this;
    var removedAction = self.mirror.getByTimestamp(timestampStr);
    if (removedAction.id){
        var removeRequest = new XHR('DELETE', '/babyactions/'+self.name+"/"+removedAction.id, function(data){
            try{
                if (this.readyState == 4) {
                    if (this.status == 200){
                        var result = JSON.parse(this.responseText);
                        if (!result.error){
                            self.mirror.remove(timestampStr);
                            self.listView.createTable(self.mirror.days);
                        } else {
                            humane.log('Unable to remove element from the server '+result.message);
                        }
                    } else {
                        humane.log('Unable to remove element from the server, probably no connection to the server.');
                    }
                }
            } catch (err){
                console.log('Unable to receive data - using local cache ', err);
            }
        });
        removeRequest.send();
    } else {
        self.store.remove(timestampStr);
        self.mirror.getByTimestamp(timestampStr);
        self.listView.createTable(self.mirror.days);
    }
}

UI.prototype.reset = function(){
    this.mirror.reset();
    this.store.reset();
    this.refresh();
}

UI.prototype.goOnline = function(){
    this.offline = false;
    document.getElementById('networkstatus').innerText = 'online';
    this.uploadLocalActions();
};

UI.prototype.goOffline = function(){
    this.offline = true;
    document.getElementById('networkstatus').innerText = 'offline';
};