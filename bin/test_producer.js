/**
 * Created by atrimble on 11/18/2014.
 */

var speaker = require('../bigio/Speaker');
var logger = require('winston');

var message = {
    content: 'Hello World!'
};

speaker.initialize(function() {
    setTimeout(function() {
        logger.info('Sending test message.');
        speaker.send('JSTestTopic', '.*', message, 0);
    }, 2000);
});
