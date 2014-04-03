function moveMarker(aMap, aMarker, aId, aLon, aLat) {
    if (aMarker != null) {
    	// Google.v3 uses EPSG:900913 as projection, so we have to
        // transform our coordinates
        var newLonLat = new OpenLayers.LonLat(aLon, aLat).transform(
                new OpenLayers.Projection("EPSG:4326"),
                aMap.getProjectionObject());
	    var newPx = aMap.getLayerPxFromLonLat(newLonLat);
	    aMarker.moveTo(newPx);
    }
    return aMarker;
}
function createMarker(aMap, aId, aIdx, aLon, aLat) {
	// Google.v3 uses EPSG:900913 as projection, so we have to
    // transform our coordinates
    var newLonLat = new OpenLayers.LonLat(aLon, aLat).transform(
            new OpenLayers.Projection("EPSG:4326"),
            aMap.getProjectionObject());
	var size = new OpenLayers.Size(32,32);
    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
    var icon = new OpenLayers.Icon('/markers/marker'+aIdx+'.png',size,offset);
    var marker = new OpenLayers.Marker(newLonLat,icon);
    marker.map = aMap;
    marker.id = aId;
    aMap.getLayer("Markers").addMarker(marker);
    return marker;
}
function initMap() {
	var map = new OpenLayers.Map("map", {
				numZoomLevels: 19
			});
	var gsat = new OpenLayers.Layer.Google(
	        "Google Satellite",
	        {type: google.maps.MapTypeId.SATELLITE}
	    );
	map.addLayer(gsat);
    var markers = new OpenLayers.Layer.Markers( "Markers" );
    markers.id = "Markers";
    map.addLayer(markers);
    // Google.v3 uses EPSG:900913 as projection, so we have to
    // transform our coordinates
    map.setCenter(new OpenLayers.LonLat(-38.4798, -3.8093).transform(
        new OpenLayers.Projection("EPSG:4326"),
        map.getProjectionObject()
    ), 17);
    return map;	
}
function findMarker(id, layer) {
	var marker = null
	// search the marker in the layer's markers
	$.each(layer.markers, function(intIndex, objValue){
    	if (id == objValue.id) {
    		marker = objValue
    	}
	})
	return marker
}
function msgClear(msg, layer) {
	var marker = findMarker(msg.id, layer)
	if (marker != null) {
		layer.removeMarker(marker)
		marker.destroy()
	}
}
function msgMove(msg, map, layer) {
	var marker = findMarker(msg.id, layer)
    if (marker == null) {
    	// create the marker
    	marker = createMarker(map, msg.id, msg.idx,
    			msg.longitude, msg.latitude)
    } 
    marker = moveMarker(map, marker, msg.id, msg.longitude, msg.latitude)
}
function log(msg) {
	$("#log").prepend("<p>"+msg+"</p>")
}
$(document).ready(function() {
	// hideable alert thanks to Twitter Bootstrap
	$(".alert").alert()
	// map initialization
	var map = initMap()
	var markers = map.getLayer("Markers")
	// open a WebSocket
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var mapSocket = new WS("ws://"+window.location.hostname+":6696/find/ws")
	mapSocket.onmessage = function(event) {
		log(event.data)
        var msg = JSON.parse(event.data)
        if (msg.clear != null) msgClear(msg.clear, markers)
        if (msg.move != null) msgMove(msg.move, map, markers)
    }
	// if errors on websocket
	var onalert = function(event) {
        $(".alert").removeClass("hide")
        $("#map").addClass("hide")
        log("websocket connection closed or lost")
    }
	mapSocket.onerror = onalert
	mapSocket.onclose = onalert
})
