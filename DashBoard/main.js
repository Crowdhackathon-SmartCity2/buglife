require("firebase/database");


var map;

var firebase = new Firebase('https://ne-7aac7.firebaseio.com/');

//data to send to firebase
var data = {
    sender: null,
    timestamp: null,
    lat: null,
    lng: null
};

// Firebase initialaization
var config = {
    apiKey: "AIzaSyAdMZd6mAYpeiupJbzSsvRsqaI_EAiNd4E",
    authDomain: "ne-7aac7.firebaseapp.com",
    databaseURL: "https://ne-7aac7.firebaseio.com",
    projectId: "ne-7aac7",
    storageBucket: "ne-7aac7.appspot.com"
};
firebase.initializeApp(config);

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
        map.setZoom(8);
        map.setCenter(marker.getPosition());
    });
}