var map;

var messagesRef = new Firebase("https://ne-7aac7.firebaseio.com/");

function writeUserData() {
    messagesRef.push({ "TEST123TEST": 1555151 });
}

function initMap() {
    var myLatlng = { lat: -25.363, lng: 131.044 };

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
        writeUserData();
    });
}