/**
 * Created by atrimble on 11/18/2014.
 */

var bigio = require('../bigio/bigio');
var logger = require('winston');

bigio.initialize(function() {
    bigio.addListener('JSTestTopic', ".*", function(message) {
        logger.info('Received message: ');
        for(var key in Object.keys(message)) {
            logger.info(key + ' -> ' + message[key]);
        }
    });
});
