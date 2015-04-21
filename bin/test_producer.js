/**
 * Created by atrimble on 11/18/2014.
 */

var bigio = require('../bigio/bigio');
var logger = require('winston');

var message = {
    content: 'Hello World!',
    other: {
        _d: "test"
    }
};

bigio.initialize(function() {
    setTimeout(function() {
        logger.info('Sending test message.');
        bigio.send({
            topic: 'JSTestTopic',
            message: message
        });
    }, 2000);
});
