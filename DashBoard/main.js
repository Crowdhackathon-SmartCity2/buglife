var map;
var markers = [];

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

    google.maps.event.addListener(map, "click", function (event) {

        //lat and lng is available in e object
        var lat = event.latLng.lat();
        var lng = event.latLng.lng();

        var formatedLatLng = (lat + "").replace('.', 'a') + 'b' + (lng + "").replace('.', 'a');
        addWaypoint(formatedLatLng, 0);

        var marker = new google.maps.Marker({
            position: event.latLng,
            map: map
        });
        markers.push(marker);

        marker.addListener('click', function () {
            map.setZoom(8);
            map.setCenter(marker.getPosition());
        });
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
}

function addWaypoint(waypoint, percentDisabled) {
    messagesRef.child("Waypoints").update({ [waypoint]: percentDisabled });
}