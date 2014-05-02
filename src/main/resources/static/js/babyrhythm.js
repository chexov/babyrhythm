function init(){
    var ui = new UI(name);

    /*
     * Action listeners for big buttons
     */
    var actions = document.getElementsByClassName("babyAction");
    for (var i=0;i<actions.length;i++){
        actions[i].addEventListener("click", function(evt){
            evt.preventDefault();
            evt.stopPropagation();
            
            for(var k=0;k<actions.length;k++){
                actions[k].classList.remove("active");
            }
            var button = this;
            button.classList.add("active");
            
            ui.action(button.id);
            
            return false;
        });
    }
    
    window.addEventListener("online", function(){
        ui.goOnline();
    }, true);
    
    window.addEventListener("offline", function(){
        ui.goOffline();
    }, true);
    
    /*
     * Action listeners for tabs
     */
    var tabs = document.getElementsByClassName("viewTab");
    for (var i=0;i<tabs.length;i++){
        tabs[i].addEventListener("click", function(evt){
            evt.preventDefault();
            evt.stopPropagation();
            for (var i=0;i<tabs.length;i++){
                tabs[i].classList.remove("active");
            }
            this.classList.add("active");
            ui.toggleView(this.id);
        });
    }
    
    /*
     * Initialize date-time picker
     */
    if (isDatetimeTypeSupported()){
        var input = document.getElementById("activityTime");
        input.addEventListener("change", function(evt){
            try {
                if (evt.target.valueAsDate)
                    ui.actionDate = evt.target.valueAsDate;
            } catch (e){
                if (window.console)
                    console.log(e);
            }
        });
        
    } else {
        loadjsfile("/static/js/jquery-1.8.3.js", function(){
            loadjsfile("/static/js/datetimepicker.js", function(){
                loadcssfile("/static/css/datetimepicker.css", function(){
                    $("#activityTime").attr("readonly", "readonly");
                    $("#activityTime").datetimepicker({
                        format: "dd MM yyyy - hh:ii",
                        autoclose: true,
                        todayBtn: true
                    }).on('changeDate', function(ev){
                        if (ev.date){
                            ui.convertAndSetActionDate(ev.date);
                        }
                    });
                    
                    $("#activityTime").datetimepicker('setDate', new Date());
                });
            });
        });
    }
    
    /*
     * Action listener for "manual add" button
     */ 
    document.getElementById("addAction").addEventListener("click", function(evt){
        ui.addAction();
    });
    
    document.getElementById("reset").addEventListener("click", function(evt){
        evt.preventDefault();
        evt.stopPropagation();
        ui.reset();
        toastr.info("All local info was erased. Refresh the page to download anew.");
        return false;
    });
    
    /*
     * Get initial data for views
     */
    var daysXHR = new XHR('GET', '/babyactions/'+name, function(data) {
        try{
            if (this.readyState == 4 && this.status == 200) {
                var actions = JSON.parse(this.responseText);                    
                
                for (var i=0;i<actions.length;i++){
                    ui.mirror.addAction(actions[i]);
                }
                ui.mirror.persist();
                ui.chartView.drawAllDays(ui.mirror.days);
            }
        } catch (err){
            console.log('Unable to receive data - using local cache ', err);
        }
    });
    daysXHR.send();
}

function loadjsfile(filename, callback) {
    var fileref = document.createElement('script');
    fileref.setAttribute("type", "text/javascript");
    fileref.setAttribute("src", filename);
    fileref.onload = function () {
        callback();
    };

    if (typeof fileref != "undefined")
        document.getElementsByTagName("head")[0].appendChild(fileref);
}

function loadcssfile(filename, callback) {
    var fileref = document.createElement("link")
    fileref.setAttribute("rel", "stylesheet")
    fileref.setAttribute("type", "text/css")
    fileref.setAttribute("href", filename)
    fileref.onload = function () {
        callback();
    };
    if (typeof fileref != "undefined")
        document.getElementsByTagName("head")[0].appendChild(fileref)
}



function isDatetimeTypeSupported() {
    // Create element
    var input = document.createElement("input");
    // attempt to set the specified type
    input.setAttribute("type", "datetime");
    // If the "type" property equals "text"
    // then that input type is not supported
    // by the browser
    var val = (input.type !== "text");
    // Delete "input" variable to
    // clear up its resources
    delete input;
    // Return the detected value
    return val;
}