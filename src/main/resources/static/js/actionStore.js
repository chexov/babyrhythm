ActionStore = function(name){
    this.name = name;
    this.key = this.name+" babyrhythm.new.actions";
	
	if (ActionStore.isHTML5StoreAvailable()){
		var jsonActions = localStorage.getItem(this.key);
		if (jsonActions){
			this.localActions = JSON.parse(jsonActions);
			console.log("Local storage enabled, stored actions: "+jsonActions);
		}
	}
};

ActionStore.isHTML5StoreAvailable = function() {
	try {
		return 'localStorage' in window && window['localStorage'] !== null;
	} catch (e) {
		return false;
	}
};

ActionStore.prototype.html5 = ActionStore.isHTML5StoreAvailable();
ActionStore.prototype.localActions = [];


ActionStore.prototype.push = function(action){
	this.localActions.push(action);
	if (this.html5)
		localStorage.setItem(this.key, JSON.stringify(this.localActions));
	console.log(this.localActions);
};

ActionStore.prototype.pop = function(){
	var action = this.localActions.pop();
	if (this.html5)
		localStorage.setItem(this.key, JSON.stringify(this.localActions));
	return action;
};

ActionStore.prototype.reset = function(){
    if (this.html5)
        localStorage.setItem(this.key, "");
    this.localActions = [];
};

ActionStore.prototype.remove = function(timestampStr){
    var idx = 0;
    var ts = parseInt(timestampStr);
    while (idx < this.localActions.length){
        if (this.localActions[idx].timestamp == ts)
            break;
        idx++;
    }
    if (idx >= this.localActions.length)
        return undefined;
    
    return this.localActions.splice(idx, 1);
}

ActionStore.prototype.size = function(){
	return this.localActions.length;
};
