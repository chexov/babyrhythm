LocalActionsMirror = function(name){
    this.name = name;
    this.days = LocalActionsMirror.getDaysFromLocalStorage(this.name); 
};

LocalActionsMirror.getDaysFromLocalStorage = function(name){
    if (!ActionStore.isHTML5StoreAvailable())
        return {};
        
    if (localStorage.getItem(name+" babyrhythm.days")){
        return JSON.parse(localStorage.getItem(name+" babyrhythm.days"));
    } else {
        return {};
    }
};

LocalActionsMirror.prototype.addAction  = function(anAction){    
    var d = new Date(anAction.timestamp);
    var date = d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear();
    var time = d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()+":"+d.getMilliseconds();
    if (!this.days[date]){
        this.days[date] = {};
    }
    this.days[date][time] = anAction;
};

LocalActionsMirror.prototype.persist = function(){
    if (!ActionStore.isHTML5StoreAvailable())
        return false;
        
    localStorage.setItem(this.name+" babyrhythm.days", JSON.stringify(this.days));
    return true;
};

LocalActionsMirror.prototype.getByTimestamp = function(timestampStr){
    var timestamp = parseInt(timestampStr);
    var d = new Date(timestamp);
    var date = d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear();
    var time = d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()+":"+d.getMilliseconds();
    if (this.days[date] && this.days[date][time])
        return this.days[date][time];
    
    return undefined;
}

LocalActionsMirror.prototype.remove = function(timestampStr){
    function size(obj) {
        var size = 0, key;
        for (key in obj) {
            if (obj.hasOwnProperty(key)) size++;
        }
        return size;
    };
    var timestamp = parseInt(timestampStr);
    var d = new Date(timestamp);
    var date = d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear();
    var time = d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()+":"+d.getMilliseconds();
    if (this.days[date] && this.days[date][time]){
        var aDay = this.days[date];
        var removedAction = aDay[time];
        aDay[time] = undefined;
        delete aDay[time];
        if (size(aDay) == 0){
            delete this.days[date];
        }
        return removedAction;
    }
    
    return undefined;
}

LocalActionsMirror.prototype.reset = function(){
    if (ActionStore.isHTML5StoreAvailable())
        localStorage.setItem(this.name+" babyrhythm.days", "");
    
    this.days = {};
}
