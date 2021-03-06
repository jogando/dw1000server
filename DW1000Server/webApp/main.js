var ratioCoordinateToPx = 100;

var anchorColor = "#00A308";
var tagColor = "#FF1C0A";
var rangeReportColor = "rgba(135, 206, 250, .3)";
var scene;
var listTagPositions;

var isRunning = false;

function load()
{
    requestScene();
    setInterval(function(){
    	if(isRunning)
   		{
    		requestTagPositions();
   		}
    }, 500);
}

function getPxFromCoordinate(coordinate)
{
    return coordinate * ratioCoordinateToPx;
}

function requestScene()
{
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState == 4 && xhttp.status == 200) {
            scene = JSON.parse(xhttp.responseText);
            createScene();
            renderScene();
        }
    }
    xhttp.open("GET", "/scene", false);//SYNC!!!!!
    xhttp.send();
}

function requestTagPositions()
{
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState == 4 && xhttp.status == 200) {

       		listTagPositions = JSON.parse(xhttp.responseText);

        	renderScene();
        }
    }
    xhttp.open("GET", "/tag?a=listAllPositions", true);
    xhttp.send();
}

function createScene()
{
    //create the canvas
    var width = getPxFromCoordinate(scene.endX);
    var height = getPxFromCoordinate(scene.endY);
    var html = "<canvas id='canvas' width='" + width + "' height='" + height + "' style='border: 1px black solid;'></canvas>";

    document.getElementById("canvasContainer").innerHTML = html;
}

function getAnchorById(id)
{
	var result = null;
	
	for(var i=0;i<scene.listAnchors.length;i++)
	{
		if(scene.listAnchors[i].id == id)
		{
			result = scene.listAnchors[i];
			break;
		}
	}
	
	return result;
}

function renderScene()
{
    var ctx = document.getElementById("canvas").getContext("2d");
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);//limpiar lo anterior
    
    for (var i = 0; i < scene.listAnchors.length ; i++)
    {
        renderAnchor(ctx, scene.listAnchors[i]);
    }
    
    if(listTagPositions != null)
   	{
    	document.getElementById("divInfo").innerHTML = "";
    	for (var i = 0; i < listTagPositions.length ; i++)
        {
    		
    		
    		if(listTagPositions[i].listRangeReports != null)// this means that the trilateration was performed, so we can place the tag in the map
			{
    			renderRangeReport(ctx,listTagPositions[i]);
			}
    		if(listTagPositions[i].coordinates != null)// this means that the trilateration was performed, so we can place the tag in the map
			{
    			renderTag(ctx, listTagPositions[i]);
			}
    		
            renderInfo(listTagPositions[i]);
        }
   	}
    
}

function renderAnchor(ctx, anchor)
{
    var anchorRadius = 0.1;
    drawCircle(
        ctx,
        getPxFromCoordinate(anchor.coordinates.x),
        getPxFromCoordinate(anchor.coordinates.y), 
        getPxFromCoordinate(anchorRadius),
        anchorColor
    );
}

function renderTag(ctx, tag)
{
    var tagRadius = 0.06;
    drawCircle(
        ctx,
        getPxFromCoordinate(tag.coordinates.x),
        getPxFromCoordinate(tag.coordinates.y), 
        getPxFromCoordinate(tagRadius),
        tagColor
    );
}

function renderRangeReport(ctx, tagPosition)
{
	for(var i=0;i<tagPosition.listRangeReports.length;i++)
	{
		var rangeReport = tagPosition.listRangeReports[i];
		var rangeRadius = rangeReport.distance * ratioCoordinateToPx;
	    var anchor = getAnchorById(rangeReport.anchorId);
	    drawCircle(
	        ctx,
	        getPxFromCoordinate(anchor.coordinates.x),
	        getPxFromCoordinate(anchor.coordinates.y), 
	        rangeRadius,
	        rangeReportColor
	    );
	}
    
}

function drawCircle(ctx, x, y, r, fillColor)
{
    ctx.beginPath();
    ctx.arc(x, y, r, 0, Math.PI * 2, true);
    ctx.closePath();
    ctx.fillStyle = fillColor;
    ctx.fill();
}

function drawRectangle(ctx, x, y, width, height, fillColor)
{
    ctx.fillStyle = fillColor;
    ctx.beginPath();
    ctx.rect(x, y, width, height);
    ctx.closePath();
    ctx.fill();
}

function renderInfo(tagPosition)
{
	var html = "<b>tagId: "+tagPosition.tag.id+"</b></br>";
	if(tagPosition.coordinates == null)
	{
		html+="not enough anchors for trilaterating...<br>";
	}
	else
	{
		html+="x:"+tagPosition.coordinates.x+"<br>";
		html+="y:"+tagPosition.coordinates.y+"<br>";
	}
	
	for(var i=0;i<tagPosition.listRangeReports.length;i++)
	{
		html+="distance from "+tagPosition.listRangeReports[i].anchorId+":     "
			+tagPosition.listRangeReports[i].distance+" m<br>";
	}
	html +="<br>";
	document.getElementById("divInfo").innerHTML += html;
}

function btnRunning_click()
{
	isRunning = !isRunning;
	if(isRunning)
	{
		document.getElementById("btnRunning").value = "stop";
	}
	else
	{
		document.getElementById("btnRunning").value = "start";
	}
}