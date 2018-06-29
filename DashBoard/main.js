var map;

var messagesRef = new Firebase("https://ne-7aac7.firebaseio.com/");


function initMap() {
    var myLatlng = { lat: 37.946966, lng: 23.723573 };

    var map = new google.maps.Map(document.getElementById('map'), {
        zoom: 4,
        center: myLatlng
    });

    var marker = new google.maps.Marker({
        position: myLatlng,
        map: map,
        title: 'Click to zoom'
    });

    marker.addListener('click', function () {
        for (i  = 0; i < 10; i++) {
            addWaypoint("123a321b32a21c543a34b23a513"+i, 5+i);
        }
    });
}

function addWaypoint(waypoint, percentDisabled) {
    messagesRef.child("Waypoints").update({ [waypoint]:percentDisabled });
}