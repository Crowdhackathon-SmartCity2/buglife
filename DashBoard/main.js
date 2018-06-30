var map;

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
    //map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);
    var bounds = new google.maps.LatLngBounds();

    var marker = new google.maps.Marker({
        position: myLatlng,
        map: map,
        title: 'Click to zoom'
    });

    marker.addListener('click', function () {
        for (i = 0; i < 10; i++) {
            addWaypoint("123a321b32a21c543a34b23a513" + i, 5 + i);
        }
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