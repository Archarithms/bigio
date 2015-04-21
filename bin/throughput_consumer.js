/**
 * Created by atrimble on 4/21/2015.
 */

var bigio = require('../bigio/bigio');
var logger = require('winston');

bigio.initialize(function() {
    bigio.addListener({
        topic: 'ThroughputConsumer',
        listener: function(message) {
            bigio.send({
                topic: 'ThroughputProducer',
                message: message
            });
        }
    });
});