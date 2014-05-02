ChartView = function(parentElementId){
    this.parentElement = document.getElementById(parentElementId);
};

ChartView.prototype.drawBabyDay = function(dayCanvas, babyDay, lastActivityFromPreviousDay){
    var ctx = dayCanvas.getContext('2d');
    if (lastActivityFromPreviousDay){
        ctx.fillStyle = pickColors(lastActivityFromPreviousDay.type);
        ctx.fillRect(0, 0, dayCanvas.width, dayCanvas.height);
    }
    var activity = null;
    for ( var i = 0; i < babyDay.length; i++) {
        activity = babyDay[i];
        var hourMinute = new Date(activity.timestamp);
        var startBar = Math.round((hourMinute.getHours() * 60 + hourMinute.getMinutes())/2);
        var barWidth = dayCanvas.width - startBar;
        
        ctx.clearRect(startBar, 0, barWidth, dayCanvas.height);
        ctx.fillStyle = this.pickColors(activity.type);
        ctx.fillRect(startBar, 0, barWidth, dayCanvas.height);
    }

    ctx.strokeStyle = "rgb(0,0,0)";
    for ( var i = 30; i < dayCanvas.width; i += 30) {
        ctx.beginPath();
        ctx.lineWidth = 1;
        ctx.moveTo(i, 0);
        ctx.lineTo(i, dayCanvas.height);
        ctx.closePath();
        ctx.stroke();
    }

    for ( var i = 7.5; i < dayCanvas.width; i += 7.5) {
        if (i % 30 != 0) {
            ctx.strokeStyle = "rgba(15,15,15,0.5)";
            ctx.beginPath();
            var st = Math.round(i);
            this.dashedLine(ctx, st, 0, st, dayCanvas.height, [ 20, 10 ]);
            ctx.closePath();
            ctx.stroke();
        }
    }    
};

ChartView.prototype.drawAllDays = function(days){
    
    while (this.parentElement.hasChildNodes()) {
        this.parentElement.removeChild(this.parentElement.lastChild);
    }
    
    var lastActivityFromPreviousDay = undefined;
    for(var i in days){
        console.log(i);
        var currentDayCanvas = document.createElement('canvas');
        currentDayCanvas.width = '720';
        currentDayCanvas.height = '50';
        currentDayCanvas.style.width = '720px';
        currentDayCanvas.style.height = '50px';
        currentDayCanvas.id = 'day'+i;
        var timesObj = days[i];
        var times = [];
        
        for (var j in timesObj)
            times.push(timesObj[j]);
        times.sort(function(a,b){
            return a.timestamp - b.timestamp;
        });
        
        this.drawBabyDay(currentDayCanvas, times, lastActivityFromPreviousDay);
        this.drawDate(currentDayCanvas, i);
        this.parentElement.appendChild(currentDayCanvas);
        
        lastActivityFromPreviousDay = times[times.length-1];
    }
};

ChartView.prototype.drawDate = function(canvas, date){
    var ctx = canvas.getContext('2d');
    ctx.fillStyle = "#000";
    ctx.font = "bold 12px 'Istok Web'";
    ctx.fillText(date, 10, 15);
};

ChartView.prototype.pickColors = function (action){
    var colors = {
        "sleep"  : "rgba(0,0,150,0.5)",
        "feed"   : "green",
        "eat"   : "green",
        "fussy"  : "red",
        "change" : "orange",
        "awake"  : "blue",
    };
    if (colors[action])
        return colors[action];
    
    return "black";
};

ChartView.prototype.dashedLine =  function(context, x, y, x2, y2, dashArray) {
    if (!dashArray)
        dashArray = [ 10, 5 ];
    var dashCount = dashArray.length;
    var dx = (x2 - x);
    var dy = (y2 - y);
    var xSlope = (Math.abs(dx) > Math.abs(dy));
    var slope = (xSlope) ? dy / dx : dx / dy;

    context.moveTo(x, y);
    var distRemaining = Math.sqrt(dx * dx + dy * dy);
    var dashIndex = 0;
    while (distRemaining >= 0.1) {
        var dashLength = Math.min(distRemaining, dashArray[dashIndex
                % dashCount]);
        var step = Math.sqrt(dashLength * dashLength / (1 + slope * slope));
        if (xSlope) {
            if (dx < 0)
                step = -step;
            x += step
            y += slope * step;
        } else {
            if (dy < 0)
                step = -step;
            x += slope * step;
            y += step;
        }
        context[(dashIndex % 2 == 0) ? 'lineTo' : 'moveTo'](x, y);
        distRemaining -= dashLength;
        dashIndex++;
    }
};

function pickColors(action){
    var colors = {
        "sleep"  : "rgba(0,0,150,0.5)",
        "feed"   : "green",
        "eat"   : "green",
        "fussy"  : "red",
        "change" : "orange",
        "awake"  : "blue",
    };
    if (colors[action])
        return colors[action];
    
    return "black";
}

function dashedLine(context, x, y, x2, y2, dashArray) {
	if (!dashArray)
		dashArray = [ 10, 5 ];
	var dashCount = dashArray.length;
	var dx = (x2 - x);
	var dy = (y2 - y);
	var xSlope = (Math.abs(dx) > Math.abs(dy));
	var slope = (xSlope) ? dy / dx : dx / dy;

	context.moveTo(x, y);
	var distRemaining = Math.sqrt(dx * dx + dy * dy);
	var dashIndex = 0;
	while (distRemaining >= 0.1) {
		var dashLength = Math.min(distRemaining, dashArray[dashIndex
				% dashCount]);
		var step = Math.sqrt(dashLength * dashLength / (1 + slope * slope));
		if (xSlope) {
			if (dx < 0)
				step = -step;
			x += step
			y += slope * step;
		} else {
			if (dy < 0)
				step = -step;
			x += slope * step;
			y += step;
		}
		context[(dashIndex % 2 == 0) ? 'lineTo' : 'moveTo'](x, y);
		distRemaining -= dashLength;
		dashIndex++;
	}
}

function drawBabyDay(dayCanvas, babyDay, lastActivityFromPreviousDay) {
	var ctx = dayCanvas.getContext('2d');
	if (lastActivityFromPreviousDay){
		ctx.fillStyle = pickColors(lastActivityFromPreviousDay.type);
		ctx.fillRect(0, 0, dayCanvas.width, dayCanvas.height);
	}
	var activity = null;
	for ( var i = 0; i < babyDay.length; i++) {
		activity = babyDay[i];
		var hourMinute = new Date(activity.timestamp);
		var startBar = Math.round((hourMinute.getHours() * 60 + hourMinute.getMinutes())/2);
		var barWidth = dayCanvas.width - startBar;
		
		ctx.clearRect(startBar, 0, barWidth, dayCanvas.height);
		ctx.fillStyle = pickColors(activity.type);
		ctx.fillRect(startBar, 0, barWidth, dayCanvas.height);
	}

	ctx.strokeStyle = "rgb(0,0,0)";
	for ( var i = 30; i < dayCanvas.width; i += 30) {
		ctx.beginPath();
		ctx.lineWidth = 1;
		ctx.moveTo(i, 0);
		ctx.lineTo(i, dayCanvas.height);
		ctx.closePath();
		ctx.stroke();
	}

	for ( var i = 7.5; i < dayCanvas.width; i += 7.5) {
		if (i % 30 != 0) {
			ctx.strokeStyle = "rgba(15,15,15,0.5)";
			ctx.beginPath();
			var st = Math.round(i);
			dashedLine(ctx, st, 0, st, dayCanvas.height, [ 20, 10 ]);
			ctx.closePath();
			ctx.stroke();
		}
	}
}

function drawAllDays(days, parentElement){
    var lastActivityFromPreviousDay = undefined;
	for(var i=0; i<days.length;i++){
		var currentDayCanvas = document.createElement('canvas');
		currentDayCanvas.width = '720';
		currentDayCanvas.height = '50';
		currentDayCanvas.style.width = '720px';
		currentDayCanvas.style.height = '50px';
		currentDayCanvas.id = 'day'+i;
		var currentDay = days[i];
        var times = currentDay.times;
		drawBabyDay(currentDayCanvas, times, lastActivityFromPreviousDay);
		drawDate(currentDayCanvas, currentDay);
		parentElement.appendChild(currentDayCanvas);
		
		lastActivityFromPreviousDay = times[times.length-1];
	}
}

function drawDate(canvas, babyDay){
    var date = babyDay.day+"/"+(babyDay.month+1)+"/"+babyDay.year;
    var ctx = canvas.getContext('2d');
    ctx.fillStyle = "#000";
    ctx.font = "bold 12px 'Istok Web'";
    ctx.fillText(date, 10, 15);
}