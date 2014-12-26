/**
 * Created by atrimble on 11/18/2014.
 */

var speaker = require('../bigio/Speaker');
var logger = require('winston');

speaker.initialize(function() {
    speaker.addListener('JSTestTopic', ".*", function(message) {
        logger.info('Received message: ');
        for(var key in Object.keys(message)) {
            logger.info(key + ' -> ' + message[key]);
        }
    });
});
