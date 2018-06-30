var map;
var markerID = 0;
var markerSelected = 0;
var markers = [];
var crashes = [];
var crashMarkers = [];
var roads = [];
var roadMarkers = [];

var roadMarkerContentString = '<button class="markerRemove">X</button><button class="send">Done</button><br><p style="color: black">Leof. Andrea Siggrou</p>';


var messagesRef = new Firebase("https://ne-7aac7.firebaseio.com/");


function initMap() {
    var myLatlng = { lat: 37.946966, lng: 23.723573 };
    var map = new google.maps.Map(document.getElementById('map'), {
        zoom: 4,
        center: myLatlng
    });
    var input = document.getElementById('pac-input');

    var searchBox = new google.maps.places.SearchBox(input);
    var places = searchBox.getPlaces();
    var bounds = new google.maps.LatLngBounds();

    var infowindow = new google.maps.InfoWindow({
        content: roadMarkerContentString
    });

    google.maps.event.addListener(map, "click", function (event) {
        var marker = new google.maps.Marker({
            position: event.latLng,
            map: map
        });
        marker.set("id", markerID);
        markers.push([marker, markerID]);

        marker.addListener('click', function () {
            infowindow.open(map, marker);
            markerSelected = marker.get("id");
        });
        markerID++;
    });

    // Bias the SearchBox results towards current map's viewport.
    map.addListener('bounds_changed', function () {
        searchBox.setBounds(map.getBounds());
    });

    searchBox.addListener('places_changed', function () {
        var places = searchBox.getPlaces();

        if (places.length == 0) {
            return;
        }

        // For each place, get the icon, name and location.
        var bounds = new google.maps.LatLngBounds();
        places.forEach(function (place) {
            if (!place.geometry) {
                console.log("Returned place contains no geometry");
                return;
            }
            var icon = {
                url: place.icon,
                size: new google.maps.Size(71, 71),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(17, 34),
                scaledSize: new google.maps.Size(25, 25)
            };

            if (place.geometry.viewport) {
                // Only geocodes have viewport.
                bounds.union(place.geometry.viewport);
            } else {
                bounds.extend(place.geometry.location);
            }
        });
        map.fitBounds(bounds);
    });

    messagesRef.child("Crash").on("value", function (snapshot) {
        crashes = [];
        crashes = snapshot.val();

        var icon = {
            url: "./icons/crash.png",
            scaledSize: new google.maps.Size(25, 25), // scaled size
            origin: new google.maps.Point(0, 0), // origin
            anchor: new google.maps.Point(0, 0) // anchor
        };

        for (var i = 0; i < crashMarkers.length; i++) {
            crashMarkers[i].setMap(null);
        }

        for (var propt in crashes) {
            var LatLng = crashes[propt].split(',');
            var v1 = parseFloat(LatLng[0]);
            var v2 = parseFloat(LatLng[1]);
            var nLatLng = new Object;
            nLatLng["lat"] = v1;
            nLatLng["lng"] = v2;
            var marker = new google.maps.Marker({
                position: nLatLng,
                icon: icon,
                map: map
            });
            crashMarkers.push(marker);
        }
    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
    });

    messagesRef.child("Waypoints").on("value", function (snapshot) {
        roads = [];
        roads = snapshot.val();

        console.log(roads);

        for (var i = 0; i < roadMarkers.length; i++) {
            roadMarkers[i].setMap(null);
        }

        var latlngs = Object.keys(roads);

        latlngs.forEach(element => {
            LatLng = element.replace('a','.').replace('b',',').replace('a', '.').split(',')
            var v1 = parseFloat(LatLng[0]);
            var v2 = parseFloat(LatLng[1]);
            var nLatLng = new Object;
            nLatLng["lat"] = v1;
            nLatLng["lng"] = v2;

            var marker = new google.maps.Marker({
                position: nLatLng,
                map: map
            });
            roadMarkers.push(marker); 

            var menuwindow = new google.maps.InfoWindow({
                content: roadMarkerContentString
            });
        
            google.maps.event.addListener(map, "click", function (event) {
                var marker = new google.maps.Marker({
                    position: event.latLng,
                    map: map
                });
                marker.set("id", markerID);
                markers.push([marker, markerID]);
        
                marker.addListener('click', function () {
                    menuwindow.open(map, marker);
                    markerSelected = marker.get("id");
                });
                markerID++;
            });
        });

    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
    });
}

function addWaypoint(waypoint, percentDisabled) {
    messagesRef.child("Waypoints").update({ [waypoint]: percentDisabled });
}

$(document).ready(function () {
    $(document).on('click', '.markerRemove', function () {
        //Delete the marker
        markers.forEach(function (index) {
            //search for the marker that we have last selected
            if (index[1] == markerSelected) {
                var waypointToRemove = markers[markers.indexOf(index)][0].getPosition();
                var lat = waypointToRemove.lat();
                var lng = waypointToRemove.lng();
                var formatedLatLng = (lat + "").replace('.', 'a') + 'b' + (lng + "").replace('.', 'a');

                markers[markers.indexOf(index)][0].setMap(null);
                markers.splice(markers.indexOf(index), 1);

                //delete the waypoint from the database
                messagesRef.child("Waypoints").child(formatedLatLng).remove();
            }
        });
    });

    $(document).on('click', '.send', function () {
        var LatLng;
        markers.forEach(function (index) {
            //search for the marker that we have last selected
            if (index[1] == markerSelected) {
                LatLng = markers[markers.indexOf(index)][0].internalPosition;
            }
        });
        var lat = LatLng.lat();
        var lng = LatLng.lng();

        var formatedLatLng = (lat + "").replace('.', 'a') + 'b' + (lng + "").replace('.', 'a');
        addWaypoint(formatedLatLng, 0);
    });
});