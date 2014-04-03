function GMapsInitialize(ws,lng,lat) {
  var latlng = new google.maps.LatLng(lat, lng)
  var mapOptions = {
    zoom: 17,
    center: latlng,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  }
  var map = new google.maps.Map(document.getElementById("map"), mapOptions)
  var marker = new google.maps.Marker({
    position: latlng,
    draggable:true,
    map: map,
    id: 'marker__'
  })
  google.maps.event.addListener(marker, 'dragend', function(event) {
    map.setCenter(event.latLng)
    var msg = Math.round(event.latLng.lng()*10000)/10000 + " " + Math.round(event.latLng.lat()*10000)/10000
    ws.send(msg)
    log(msg)
  })
  return map
}
function log(msg) {
	$("#log").prepend("<p>"+msg+"</p>")
}
$(document).ready(function() {
	// hidable alert thanks to Twitter Bootstrap
	$(".alert").alert()
	// open a WebSocket
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var mapSocket = new WS("ws://"+window.location.hostname+":6696/hide/ws")
	mapSocket.onopen = function(event) {
		var msg = "-38.4798 -3.8093"
		mapSocket.send(msg)
		log(msg)
	}
	// if errors on websocket
	var onalert = function(event) {
        $(".alert").removeClass("hide")
        $("#map").addClass("hide")
        log("websocket connection closed or lost")
    }
	mapSocket.onerror = onalert
	mapSocket.onclose = onalert
	// map initialization
	var map = GMapsInitialize(mapSocket, -38.4798, -3.8093)
})
